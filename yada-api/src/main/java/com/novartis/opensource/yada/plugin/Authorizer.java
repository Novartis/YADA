/**
 * 
 */
package com.novartis.opensource.yada.plugin;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Date;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.http.HttpServletRequest;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTCreationException;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.novartis.opensource.yada.ConnectionFactory;
import com.novartis.opensource.yada.YADAConnectionException;
import com.novartis.opensource.yada.YADAExecutionException;
import com.novartis.opensource.yada.YADARequest;
import com.novartis.opensource.yada.YADARequestException;
import com.novartis.opensource.yada.YADASQLException;
import com.novartis.opensource.yada.YADASecurityException;
import com.novartis.opensource.yada.util.YADAUtils;

/**
 * @author jfinn
 *
 */
public class Authorizer extends AbstractPostprocessor implements Authorization {

	/**
	 * Contains resource and allowList
	 */
	private JSONObject yadaAuthorization = new JSONObject();

	/**
	 * Contains the name of the resource we are authorizing
	 * 
	 */
	private String resource = new String();

	/**
	 * Contains the credentials of the user we are authorizing
	 * 
	 */
	private String credentials = new String();

	/**
	 * Contains the user identity data from authority
	 */
	private Object identity = new Object();

	/**
	 * Contains the app to authorize
	 */
	private String app = new String();

	/**
	 * Contains the locks to authorize
	 * 
	 */
	private JSONObject locks = new JSONObject();

	/**
	 * Contains the user grant from authority
	 */
	private Object grant = new Object();

	/**
	 * Contains the query result from authority
	 */
	private String result = new String();

	/**
	 * Contains the synchronization token
	 */
	private String syncToken = new String();

	/**
	 * Contains the list of allow qualifiers from A11N
	 */
	private ArrayList<String> allowList = new ArrayList<String>();

	/**
	 * Contains the list of deny qualifiers from A11N
	 */
	private ArrayList<String> denyList = new ArrayList<String>();

	/**
	 * Base implementation, calls {@link #setYADARequest(YADARequest)},
	 * {@link #setRequest(HttpServletRequest)}, {@link #setHTTPHeaders(String[])},
	 * {@link #authorizeYADARequest(YADARequest, String)} and returns
	 * {@link #getResult()}
	 * 
	 * @throws YADAPluginException
	 *           when there is a processing error
	 * @see com.novartis.opensource.yada.plugin.Postprocess#engage(com.novartis.opensource.yada.YADARequest,
	 *      java.lang.String)
	 */
	@Override
	public String engage(YADARequest yadaReq, String result) throws YADAPluginException, YADASecurityException {

		setResult(result);
		this.setYADARequest(yadaReq);
		this.setRequest(yadaReq.getRequest());

		// Make header available
		try {
			this.setHTTPHeaders(YADA_HDR_AUTH_NAMES);
		} catch (YADARequestException e) {
			String msg = "User is not authorized";
			throw new YADASecurityException(msg);
		}

		authorizeYADARequest(yadaReq, result);

		return getResult();
	}

	/**
	 * Authorize payload
	 * 
	 * @since 8.7.6
	 */
	@Override
	public void authorize(String payload) throws YADASecurityException {

		boolean authorized = false;
		boolean blacklist = false;

		// Check authority for identity
		try {
			setIdentity(obtainIdentity());
		} catch (YADASecurityException | YADARequestException | YADAExecutionException e) {
			String msg = "User is not authorized";
			throw new YADASecurityException(msg);
		}

		// Check request for locks
		try {
			setLocks(obtainLocks());
		} catch (YADARequestException | YADAExecutionException e2) {
			String msg = "A11N Request Exception";
			throw new YADASecurityException(msg);
		}

		if (hasLocks()) {
			JSONArray key = getLocks().names();
			for (int i = 0; i < key.length(); ++i) {
				String grant = key.getString(i);
				String listtype = getLocks().getString(grant);
				if (listtype.equals(AUTH_TYPE_WHITELIST)) {
					// Add locks to allowList
					addAllowListEntry(grant);
				} else {
					// obtainLocks() writes whitelist grants only
					// this supports the addition of blacklist grants
					removeAllowListEntry(grant);
					blacklist = true;
				}
			}
		}
		if (hasIdentity()) {
			try {
				// Obtain a relevant GRANT if it exists within IDENTITY
				setGrant(obtainGrant(getApp()));
			} catch (YADASecurityException | YADARequestException | YADAExecutionException e) {
				String msg = "User is not authorized";
				throw new YADASecurityException(msg);
			}
			// Is there a GRANT for this APP or is there a blacklist entry requiring
			// a valid lock?
			if (hasGrants() || blacklist == true) {
				if (hasAllowList()) {
					// pl=Authorizer,<APP>,<LOCK>
					for (int i = 0; i < ((JSONArray) getGrant()).length(); i++) {
						if (getAllowList().contains(((JSONArray) getGrant()).get(i).toString())) {
							authorized = true;
						}
					}
				} else {
					// pl=Authorizer,<APP>
					authorized = true;
				}
			}
		}
		if (authorized == true) {
			// Return the tokens
			setResult(generateResult().toString());
		} else {
			String msg = "User is not authorized";
			throw new YADASecurityException(msg);
		}
	}

	/**
	 * 
	 * @return 
	 * @throws JSONException
	 * @throws YADASecurityException
	 * @since 8.7.6
	 */
	public JSONObject generateResult() throws YADASecurityException, JSONException {
		JSONObject result = new JSONObject();
		// Add the sync token
		result.put(YADA_HDR_SYNC_TKN, getSyncToken());
		// Add the identity token
		result.put(YADA_HDR_AUTH_JWT_PREFIX, this.getToken());
		return result;
	}

	/**
	 * 
	 * @return yadaauth {Role: [whitelist/blacklist]}
	 * @throws YADARequestException
	 * @throws YADASecurityException
	 * @throws YADAExecutionException
	 * @since 8.7.6
	 */
	public JSONObject obtainLocks() throws YADARequestException, YADASecurityException, YADAExecutionException {
		JSONObject result = new JSONObject();
		// The first argument is the APP (required) and set
		// All following are the LOCK(S) (optional) and returned if present
		// This method sets whitelist values only
		List<String> arg = this.getYADARequest().getArgs();
		if (arg != null && !arg.isEmpty()) {
			for (int i = 0; i < arg.size(); i++) {
				if (i == 0) {
					setApp(arg.get(i));
				} else {
					result.put(arg.get(i), AUTH_TYPE_WHITELIST);
				}
			}
		}
		return result;
	}

	/**
	 * Check header for credentials, obtain identity, obtain token, and cache
	 * identity with token
	 * 
	 * @throws YADASecurityException
	 * @since 8.7.6
	 */
	@Override
	public void obtainToken(YADARequest yadaReq) throws YADASecurityException {
		// Check header for credentials
		Pattern rxAuthUsr = Pattern.compile(RX_HDR_AUTH_USR_PREFIX);
		boolean obtainedtoken = false;
		if (this.hasHttpHeaders()) {
			for (int i = 0; i < this.getHttpHeaders().names().length(); i++) {
				// Check for basic auth
				Matcher m1 = rxAuthUsr
				    .matcher((CharSequence) this.getHttpHeaders().get(this.getHttpHeaders().names().getString(i)));
				if (m1.matches() && m1.groupCount() == 3) {// valid header
					setCredentials(m1.group(3));
				}
			}
		}
		if (hasCredentials()) {
			// We have credentials
			byte[] credentialBytes = Base64.getDecoder().decode(getCredentials());
			String credentialString = new String(credentialBytes);
			String userid = new String();
			String pw = new String();
			Pattern rxAuthUsrCreds = Pattern.compile(RX_HDR_AUTH_USR_CREDS);
			Matcher m2 = rxAuthUsrCreds.matcher(credentialString);
			if (m2.matches()) {// found user
				userid = m2.group(1);
				pw = m2.group(2);
			}

			try {
				// create the sync token
				generateSyncToken(userid);
				// use credentials to retrieve user identity
				String id = obtainIdentity(userid, pw).toString();
				if (id.length() > 0) {
					// create the identity token
					generateToken(userid);
					if (hasToken()) {
						// add identity to cache using token as key
						this.setCacheEntry(YADA_IDENTITY_CACHE, (String) this.getToken(), id, YADA_IDENTITY_TTL);
						obtainedtoken = true;
					}
				}
			} catch (YADASecurityException | YADARequestException | YADAExecutionException e) {
				String msg = "User is not authorized";
				throw new YADASecurityException(msg);
			}
		}
		if (obtainedtoken == false) {
			String msg = "User is not authorized";
			throw new YADASecurityException(msg);
		}
	}

	/**
	 * @param userid 
	 * @throws YADASecurityException
	 * 
	 * @since 8.7.6
	 * 
	 */
	public void generateToken(String userid) throws YADASecurityException {
		// issueDate: JWT iat
		// expirationDate: issueDate + identity cache TTL seconds
		String token;
		Instant issueDate = Instant.now().truncatedTo(ChronoUnit.SECONDS);
		Instant expirationDate = issueDate.plus(YADA_IDENTITY_TTL, ChronoUnit.SECONDS);
		try {
			token = JWT.create().withSubject(userid).withExpiresAt(Date.from(expirationDate))
			    .withIssuer(System.getProperty(JWTISS)).withIssuedAt(Date.from(issueDate))
			    .sign(Algorithm.HMAC512(System.getProperty(JWSKEY)));
		} catch (IllegalArgumentException | JWTCreationException e ) {
			String msg = "User is not authorized";
			throw new YADASecurityException(msg);
		}
		this.setToken(token);
	}

	/**
	 * @param userid 
	 * @throws YADASecurityException
	 * 
	 * @since 8.7.6
	 * 
	 */
	public void generateSyncToken(String userid) throws YADASecurityException {
		// Create a synchronization token with the userid and the current time
		Instant issueDate = Instant.now().truncatedTo(ChronoUnit.SECONDS);
		String token = userid + issueDate.toString();
		token = String.valueOf(token.hashCode());
		setSyncToken(token);
	}

	/**
	 * Overrides {@link TokenValidator#validateToken()}. Validates JWT.
	 */

	@Override
	public void validateToken() throws YADASecurityException {
		if (hasToken()) {
			// validate token as well-formed
			try {
				JWT.require(Algorithm.HMAC512(System.getProperty(JWSKEY))).withIssuer(System.getProperty(JWTISS)).build()
				    .verify((String) this.getToken());
			} catch (JWTVerificationException | IllegalArgumentException exception) {
				// UTF-8 encoding not supported
				String msg = "Validation Error ";
				throw new YADASecurityException(msg, exception);
			}
		}
	}

	/**
	 * Obtain Identity with basic authentication
	 * @param userid 
	 * @param pw 
	 * 
	 * @return identity
	 * @throws YADARequestException
	 * @throws YADASecurityException
	 * @throws YADAExecutionException
	 * @since 8.7.6
	 * 
	 */

	public Object obtainIdentity(String userid, String pw)
	    throws YADARequestException, YADASecurityException, YADAExecutionException {
		JSONObject result = new JSONObject();
		JSONArray a = new JSONArray();
		ArrayList<String> al = new ArrayList<String>();
		// TODO: Replace prepared statement with YADA query
		try (ResultSet rs = YADAUtils.executePreparedStatement(YADA_LOGIN_QUERY, new Object[] { userid, pw });) {
			while (rs.next()) {
				String app = rs.getString(1); // YADA_LOGIN.APP
				al.add(app);
				String key = rs.getString(3); // YADA_LOGIN.ROLE (a key name)
				// a is used to get the form:
				// [{"app":app1,"key":key1},{"app":app1,"key":key2},...{"app":appN,"key":keyN}]
				a.put(new JSONObject().put(YADA_IDENTITY_APP, app).put(YADA_IDENTITY_KEY, key));
			}
			ConnectionFactory.releaseResources(rs);

			// use unique al entries and get the form:
			// [{"app":app1,"keys":[{"key":key1},{"key":key2},...{"key":keyN}]}]
			if (a.length() > 0) {
				// Distill al to applist by removing any duplicate apps
				ArrayList<String> applist = new ArrayList<String>();
				for (String app : al) {
					if (!applist.contains(app)) {
						applist.add(app);
					}
				}
				// assemble identity object
				JSONArray aid = new JSONArray();
				for (int i = 0; i < applist.size(); i++) {
					JSONArray keys = new JSONArray();
					for (int ii = 0; ii < a.length(); ii++) {
						if (applist.get(i).equals(a.getJSONObject(ii).getString(YADA_IDENTITY_APP).toString())) {
							// {YADA_IDENTITY_KEY,
							// a.getJSONObject(i).getString(YADA_IDENTITY_KEY)}
							keys.put(new JSONObject().put(YADA_IDENTITY_KEY, a.getJSONObject(i).getString(YADA_IDENTITY_KEY)));
						}
					}
					aid.put(new JSONObject().put(YADA_IDENTITY_APP, applist.get(i)).put(YADA_IDENTITY_KEYS, keys));
				}

				result.put(YADA_IDENTITY_SUB, userid);
				// Use sync token in Gatekeeper to verify the sender owns the token
				result.put(YADA_HDR_SYNC_TKN, getSyncToken());
				// a is used to get the form:
				// [{"app":app1,"grant":grant1},{"app":app1,"grant":grant2},...{"app":appN,"grant":grantN}]
				// result.put(YADA_IDENTITY_GRANTS, a);
				// aid is used to get the form:
				// [{"app":app1,"keys":[{"key":key1},{"key":key2},...{"key":keyN}]}]
				result.put(YADA_IDENTITY_GRANTS, aid);
				result.put(YADA_IDENTITY_IAT, java.time.Instant.now().getEpochSecond());
			}

		} catch (SQLException | YADAConnectionException | YADASQLException e) {
			String msg = "Unauthorized.";
			throw new YADASecurityException(msg, e);
		}
		// setIdentity(result);
		return result.toString();
	}

	/**
	 * @return 
	 * @throws YADASecurityException 
	 * @throws YADARequestException 
	 * @throws YADAExecutionException 
	 * @since 8.7.6
	 */
	public Object obtainIdentity() throws YADASecurityException, YADARequestException, YADAExecutionException {
		Object result = getCacheEntry(YADA_IDENTITY_CACHE, (String) this.getToken());
		return result;
	}

	/**
	 * Obtain specified GRANT(KEYS) from current identity
	 * @param app 
	 * @return 
	 * @throws YADASecurityException 
	 * @throws YADARequestException 
	 * @throws YADAExecutionException 
	 */
	public Object obtainGrant(String app) throws YADASecurityException, YADARequestException, YADAExecutionException {
		JSONObject jo = new JSONObject((String) getIdentity());
		JSONArray ja = jo.getJSONArray(YADA_IDENTITY_GRANTS);
		// find the app
		JSONArray keys = new JSONArray();
		for (int i = 0; i < ja.length(); i++) {
			if (app.equals(ja.getJSONObject(i).getString(YADA_IDENTITY_APP).toString())) {
				for (int ii = 0; ii < ja.getJSONObject(i).getJSONArray(YADA_IDENTITY_KEYS).length(); ii++) {
					keys.put(ja.getJSONObject(i).getJSONArray(YADA_IDENTITY_KEYS).getJSONObject(ii).getString(YADA_IDENTITY_KEY));
				}
			}
		}
		return keys;
	}

	/**
	 * @return {@code true} if {@link AbstractPostprocessor#getToken()} is set, otherwise {@code false}
	 * @throws YADASecurityException
	 * @since 8.7.6
	 */
	public boolean hasToken() throws YADASecurityException {
		if (null != this.getToken() && !"".equals(this.getToken())) {
			return true;
		}
		return false;
	}

	/**
	 * @return {@code true} if {@link #locks} has at least 1 entry, otherwise {@code false}
	 * @since 8.7.6
	 */
	public boolean hasLocks() {
		if (getLocks().length() > 0) {
			return true;
		}
		return false;
	}

	/**
	 * @return {@code true} if {@link #grant} has at least 1 entry, otherwise {@code false}
	 * @since 8.7.6
	 */
	public boolean hasGrants() {
		if (((JSONArray) getGrant()).length() > 0) {
			return true;
		}
		return false;
	}

	/**
	 * @return 
	 * @since 8.7.6
	 */
	public boolean hasIdentity() {
		if (null != getIdentity() && !"".equals(getIdentity())) {
			return true;
		}
		return false;
	}

	/**
	 * @return {@code true} if {@link #allowList} has at least 1 entry, otherwise {@code false}
	 * @since 8.7.6
	 */
	public boolean hasAllowList() {
		if (getAllowList().size() > 0) {
			return true;
		}
		return false;
	}

	/**
	 * @return {@code true} if {@link #credentials} has at least 1 entry, otherwise {@code false}
	 * @since 8.7.6
	 */
	public boolean hasCredentials() {
		if (null != getCredentials() && !"".equals(getCredentials())) {
			return true;
		}
		return false;
	}

	/**
	 * @return {@link #yadaAuthorization}
	 */
	public JSONObject getYADAAuthorization() {
		return this.yadaAuthorization;
	}

	/**
	 * @param yadaAuthorization
	 *          the yadaAuthorization to set
	 */
	public void setYADAAuthorization(JSONObject yadaAuthorization) {
		this.yadaAuthorization = yadaAuthorization;
	}

	/**
	 * @return {@link #resource}
	 */
	public String getResource() {
		return this.resource;
	}

	/**
	 * @param resource
	 *          the resource to set
	 */
	public void setResource(String resource) {
		this.resource = resource;
	}

	/**
	 * @return {@link #credentials}
	 */
	public String getCredentials() {
		return this.credentials;
	}

	/**
	 * @param credentials
	 *          the credentials to set
	 */
	public void setCredentials(String credentials) {
		this.credentials = credentials;
	}

	/**
	 * @return {@link #grant}
	 */
	public Object getGrant() {
		return this.grant;
	}

	/**
	 * @param grant
	 *          the grant to set
	 */
	public void setGrant(Object grant) {
		this.grant = grant;
	}

	/**
	 * @return {@link #identity}
	 */
	public Object getIdentity() {
		return this.identity;
	}

	/**
	 * @param identity
	 *          the identity to set
	 */
	public void setIdentity(Object identity) {
		this.identity = identity;
	}

	/**
	 * @return {@link #locks}
	 */
	public JSONObject getLocks() {
		return this.locks;
	}

	/**
	 * @param locks
	 *          the locks to set
	 */
	public void setLocks(JSONObject locks) {
		this.locks = locks;
	}

	/**
	 * @return {@link #allowList}
	 */
	public ArrayList<String> getAllowList() {
		return this.allowList;
	}

	/**
	 * @param grant
	 */
	public void addAllowListEntry(String grant) {
		this.allowList.add(grant);
	}

	/**
	 * @param grant
	 */
	public void removeAllowListEntry(String grant) {
		this.allowList.remove(grant);
	}

	/**
	 * @return the {@link #denyList}
	 */
	public ArrayList<String> getDenyList() {
		return this.denyList;
	}

	/**
	 * @param grant
	 */
	public void addDenyListEntry(String grant) {
		this.denyList.add(grant);
	}

	/**
	 * @return the {@link #app}
	 */
	public String getApp() {
		return this.app;
	}

	/**
	 * @param app
	 *          the app to set
	 */
	public void setApp(String app) {
		this.app = app;
	}

	/**
	 * @return {@link #result}
	 */
	public String getResult() {
		return this.result;
	}

	/**
	 * @param result
	 *          the result to set
	 */
	public void setResult(String result) {
		this.result = result;
	}

	/**
	 * @return {@link #syncToken}
	 */
	public String getSyncToken() {
		return this.syncToken;
	}

	/**
	 * @param syncToken
	 *          the syncToken to set
	 */
	public void setSyncToken(String syncToken) {
		this.syncToken = syncToken;
	}

}

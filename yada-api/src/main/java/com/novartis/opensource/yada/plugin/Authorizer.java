/**
 * 
 */
package com.novartis.opensource.yada.plugin;

import java.io.UnsupportedEncodingException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.json.JSONObject;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.novartis.opensource.yada.YADAExecutionException;
import com.novartis.opensource.yada.YADARequest;
import com.novartis.opensource.yada.YADARequestException;
import com.novartis.opensource.yada.YADASecurityException;

/**
 * @author jfinn
 *
 */
public class Authorizer extends AbstractPostprocessor implements Authorization {

	/**
	 * Constant equal to {@value}
	 * 
	 * @since 8.7.6
	 */
	protected static final String RX_HDR_AUTH_USR_PREFIX = "(Basic)(.+?)([a-zA-Z0-9-_.]{3,})";

	/**
	 * Constant equal to {@value}
	 * 
	 * @since 8.7.6
	 */
	protected static final String RX_HDR_AUTH_TKN_PREFIX = "(Bearer)(.+?)([a-zA-Z0-9-_.]{5,})";

	// --------------------------------------------------------------------------------
	// TODO: Change these to system properties
	// --------------------------------------------------------------------------------

	/**
	 * Array of IAM headers we want to have access to
	 */
	protected static final String[] YADA_HDR_AUTH_NAMES = { "Authorization" };

	/**
	 * Constant with value: {@value}
	 *
	 * @since 2.0
	 */
	protected final static String YADA_HDR_AUTH_USR_PREFIX = "Basic ";

	/**
	 * Constant with value: {@value}
	 *
	 * @since 8.7.6
	 */
	public final static String YADA_CK_TKN = "yadajwt";

	// --------------------------------------------------------------------------------

	/**
	 * Contains resource and allowList
	 */
	private JSONObject yadaAuthorization = new JSONObject();

	/**
	 * Contains the name of the resource we are authorizing
	 * 
	 */
	private String resource = new String();

	@Override
	public void authorize(String payload) throws YADASecurityException {
		// put standard implementation here

	}

	/**
	 * Make resources available for subclasses
	 * 
	 * @since 8.7.6
	 */
	@Override
	public void specifyYADACredentials(YADARequest yReq) throws YADASecurityException {
		// Make header available
		try {
			this.setHTTPHeaders(YADA_HDR_AUTH_NAMES);
		} catch (YADARequestException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		obtainToken(yReq);

		// validate credentials and authorize
		validateYADARequest();

	}

	/**
	 * Checking header then cookie for token to set
	 * 
	 * @throws YADASecurityException
	 */
	@Override
	public void obtainToken(YADARequest yReq) throws YADASecurityException {

		// Check header for token
		Pattern rxAuthTkn = Pattern.compile(RX_HDR_AUTH_TKN_PREFIX);

		if (this.hasHttpHeaders()) {

			for (int i = 0; i < this.getHttpHeaders().names().length(); i++) {
				Matcher m1 = rxAuthTkn
				    .matcher((CharSequence) this.getHttpHeaders().get(this.getHttpHeaders().names().getString(i)));
				if (m1.matches() && m1.groupCount() == 3) {// valid header
					this.setToken(m1.group(3));
				}
			}
		}

		// Check cookie for token
		else {
			this.setToken(getCookie(YADA_CK_TKN));
		}

		// **************************************************************************
		// TODO: Deny access with some response
		// **************************************************************************
		// if (this.getToken() == null || this.getToken().equals(""))
		// setOutcome("Unauthorized.");
		// **************************************************************************
	}

	/**
	 * Overrides {@link TokenValidator#validateToken()}.
	 *
	 * @throws YADASecurityException
	 *           when the {@link #DEFAULT_AUTH_TOKEN_PROPERTY} is not set
	 */

	@Override
	public void validateToken() throws YADASecurityException {

		if (null != this.getToken() && !"".equals(this.getToken())) {

			// validate token as well-formed
			try {
				JWT.require(Algorithm.HMAC512(System.getProperty(JWSKEY))).withIssuer(System.getProperty(JWTISS)).build()
				    .verify((String) this.getToken());
			} catch (UnsupportedEncodingException | JWTVerificationException exception) {
				// UTF-8 encoding not supported
				String msg = "Validation Error ";
				throw new YADASecurityException(msg, exception);
			}

			// Check authority for identity
			try {
				this.setIdentity(obtainIdentity());
			} catch (YADASecurityException | YADARequestException | YADAExecutionException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

	}

	/**
	 * 
	 * @return
	 * @throws YADARequestException
	 * @throws YADASecurityException
	 * @throws YADAExecutionException
	 * @since 8.7.6
	 * 
	 *        check getIdentity, if not there check cache
	 * 
	 */

	public Object obtainIdentity() throws YADASecurityException, YADARequestException, YADAExecutionException {
		Object result = getCacheEntry(YADA_IDENTITY_CACHE, (String) this.getToken());
		return result;
	}

	public Object obtainGrant(String key) throws YADASecurityException, YADARequestException, YADAExecutionException {
		// TODO Auto-generated method stub
		return null;
	}

	/**
	 * @return the yadaAuthorization
	 */
	public JSONObject getYADAAuthorization() {
		return yadaAuthorization;
	}

	/**
	 * @param yadaAuthorization
	 *          the yadaAuthorization to set
	 */
	public void setYADAAuthorization(JSONObject yadaAuthorization) {
		this.yadaAuthorization = yadaAuthorization;
	}

	/**
	 * @return the resource
	 */
	public String getResource() {
		return resource;
	}

	/**
	 * @param resource
	 *          the resource to set
	 */
	public void setResource(String resource) {
		this.resource = resource;
	}

}

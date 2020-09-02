/**
 * 
 */
package com.novartis.opensource.yada.plugin;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.attribute.PosixFilePermission;
import java.nio.file.attribute.PosixFilePermissions;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTCreationException;
import com.auth0.jwt.exceptions.JWTVerificationException;
import static com.kosprov.jargon2.api.Jargon2.*;
import com.novartis.opensource.yada.ConnectionFactory;
import com.novartis.opensource.yada.Finder;
import com.novartis.opensource.yada.YADAConnectionException;
import com.novartis.opensource.yada.YADAExecutionException;
import com.novartis.opensource.yada.YADARequest;
import com.novartis.opensource.yada.YADARequestException;
import com.novartis.opensource.yada.YADAResourceException;
import com.novartis.opensource.yada.YADASQLException;
import com.novartis.opensource.yada.YADASecurityException;
import com.novartis.opensource.yada.util.YADAUtils;

/**
 * @author jfinn
 *
 */
public class Authorizer extends AbstractPostprocessor implements Authorization {

  
  /**
   * @since 9.0.0
   */
  private static Logger l = Logger.getLogger(Authorizer.class);
  
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
   * The file containing credential hashes and grants.
   * 
   * <strong>DO NOT COMMIT THIS FILE TO THE REPOSITORY</strong>. The
   * {@code .shadow.json} file should not be renamed unless the {@code .gitignore}
   * file at the root of {@code YADA_LIB} is updated to reflect the change.
   * 
   * @since 9.0.0
   */
  private static final String YADA_SHADOW = ".shadow.json";
  
  private static final String YADA_IDENTITY_HASH = "hash";
  private static final String YADA_IDENTITY_GRANTS = "grants";
  private static final String UNAUTHORIZED = "User is not authorized";

  /**
   * Sets additional request headers
   */
  @Override
  public String engage(YADARequest yadaReq, String result) throws YADAPluginException, YADASecurityException {

    setResult(result);
    this.setYADARequest(yadaReq);
    this.setRequest(yadaReq.getRequest());

    // Make header available
    try
    {
      this.setHTTPHeaders(YADA_HDR_AUTH_NAMES);
    }
    catch (YADARequestException e)
    {      
      throw new YADASecurityException(UNAUTHORIZED);
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
   
    // Check request for locks, also sets app
    // Other than setting the app, the locks *don't really matter*
    // They don't matter at all for 9.0.0
    
    if(Finder.hasYADALib())
    {
      YADARequest yr = this.getYADARequest();    
      List<String> args = yr.getArgs();
      if(null == args || args.isEmpty())
      {
        String qname = yr.getQname();
        String app = qname.split("/")[0];
        setApp(app);      
      }
      else
      {
        setApp(args.get(0));
      }
    }
    else
    {
      JSONObject requestedLocks = obtainLocks();
      setLocks(requestedLocks);
      if (hasLocks())
      {
        JSONArray key = getLocks().names();
        for (int i = 0; i < key.length(); ++i)
        {
          String grant    = key.getString(i);
          String listtype = getLocks().getString(grant);
          if (listtype.equals(AUTH_TYPE_WHITELIST))
          {
            // Add locks to allowList
            addAllowListEntry(grant);
          }        
        }
      }
    }
    
    // Check cache for identity
    Object ident = obtainIdentity();
    setIdentity(ident);

    if (hasIdentity())
    {
      String app = this.getApp();
      if(app.equals(Finder.YADA))
      {
        authorized = true;
      }
      else
      {
        // Obtain a relevant GRANT if it exists within IDENTITY
        
        Object grant = obtainGrant(app);
        setGrant(grant);
        
        // Is there a GRANT for this APP
        if (hasGrants()) // even without locks, a grant for the app will pass
        {
  //        if (hasAllowList()) // why bother?
  //        {
  //          // pl=Authorizer,<APP>,<LOCK>
  //          JSONArray grants = (JSONArray) getGrant();
  //          for (int i = 0; i < grants.length(); i++)
  //          {
  //            String grantStr = grants.get(i).toString();
  //            if (getAllowList().contains(grantStr))
  //            {
  //              authorized = true;
  //            }
  //          }
  //        }
  //        else
  //        {
            // pl=Authorizer,<APP>
          authorized = true;
  //        }
        }
      }
    }
    
    if (authorized == true)
    {
      // Return the tokens
      setResult(generateResult().toString());
    }
    else
    {
      throw new YADASecurityException(UNAUTHORIZED);
    }
  }

  /**
   * 
   * @return
   * @since 8.7.6
   */
  public JSONObject generateResult() {
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
   * @since 8.7.6
   */
  public JSONObject obtainLocks() {
    JSONObject result = new JSONObject();
    // The first argument is the APP (required) and set
    // All following are the LOCK(S) (optional) and returned if present
    // This method sets whitelist values only
    List<String> arg = this.getYADARequest().getArgs();
    if (arg != null && !arg.isEmpty())
    {
      for (int i = 0; i < arg.size(); i++)
      {
        if (i == 0)
        {
          this.setApp(arg.get(i));
        }
        else
        {
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
    Pattern rxAuthUsr     = Pattern.compile(RX_HDR_AUTH_USR_PREFIX);
    boolean obtainedtoken = false;
    if (this.hasHttpHeaders())
    {
      for (int i = 0; i < this.getHttpHeaders().names().length(); i++)
      {
        // Check for basic auth
        Matcher m1 = rxAuthUsr
            .matcher((CharSequence) this.getHttpHeaders().get(this.getHttpHeaders().names().getString(i)));
        if (m1.matches() && m1.groupCount() == 3)
        {// valid header
          setCredentials(m1.group(3));
          break;
        }        
      }
    }
    if (hasCredentials())
    {
      // We have credentials
      byte[]  credentialBytes  = Base64.getDecoder().decode(getCredentials());
      String  credentialString = new String(credentialBytes);
      String  userid           = new String();
      String  pw               = new String();
      Pattern rxAuthUsrCreds   = Pattern.compile(RX_HDR_AUTH_USR_CREDS);
      Matcher m2               = rxAuthUsrCreds.matcher(credentialString);
      if (m2.matches())
      {// found user
        userid = m2.group(1);
        pw     = m2.group(2);
      }

      try
      {
        // create the sync token
        generateSyncToken(userid);
        // use credentials to retrieve user identity
        String id = obtainIdentity(userid, pw).toString();
        if (id.length() > 0)
        {
          // create the identity token
          generateToken(userid);
          if (hasToken())
          {
            // add identity to cache using token as key
            this.setCacheEntry(YADA_IDENTITY_CACHE, (String) this.getToken(), id, YADA_IDENTITY_TTL);
            obtainedtoken = true;
          }
        }
      }
      catch (YADASecurityException | YADARequestException | YADAExecutionException e)
      {
        throw new YADASecurityException(UNAUTHORIZED);
      }
      catch (YADAResourceException e)
      {
        String msg = "Unable to load credenitial store";
        throw new YADASecurityException(msg, e);
      }
    }
    if (obtainedtoken == false)
    {
      throw new YADASecurityException(UNAUTHORIZED);
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
    String  token;
    Instant issueDate      = Instant.now().truncatedTo(ChronoUnit.SECONDS);
    Instant expirationDate = issueDate.plus(YADA_IDENTITY_TTL, ChronoUnit.SECONDS);
    try
    {
      token = JWT.create().withSubject(userid).withExpiresAt(Date.from(expirationDate))
          .withIssuer(System.getProperty(JWTISS)).withIssuedAt(Date.from(issueDate))
          .sign(Algorithm.HMAC512(System.getProperty(JWSKEY)));
    }
    catch (IllegalArgumentException | JWTCreationException e)
    {
      throw new YADASecurityException(UNAUTHORIZED);
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
    String  token     = userid + issueDate.toString();
    token = String.valueOf(token.hashCode());
    setSyncToken(token);
  }

  /**
   * Overrides {@link TokenValidator#validateToken()}. Validates JWT.
   */

  @Override
  public void validateToken() throws YADASecurityException {
    if (hasToken())
    {
      // validate token as well-formed
      try
      {
        JWT.require(Algorithm.HMAC512(System.getProperty(JWSKEY))).withIssuer(System.getProperty(JWTISS)).build()
            .verify((String) this.getToken());
      }
      catch (JWTVerificationException | IllegalArgumentException exception)
      {
        // UTF-8 encoding not supported
        String msg = "Validation Error ";
        throw new YADASecurityException(msg, exception);
      }
    }
  }

  /**
   * Obtain Identity with basic authentication
   * 
   * @param userid
   * @param pw
   * 
   * @return identity
   * @throws YADARequestException
   * @throws YADASecurityException
   * @throws YADAExecutionException
   * @throws YADAResourceException
   * @since 8.7.6
   * 
   */

  public Object obtainIdentity(String userid, String pw)
      throws YADARequestException, YADASecurityException, YADAExecutionException, YADAResourceException {
    JSONObject        result = new JSONObject();
    JSONArray         a      = new JSONArray();
    ArrayList<String> al     = new ArrayList<String>();
    JSONArray         aid    = new JSONArray();
    // TODO: Replace prepared statement with YADA query

    if (Finder.hasYADALib())
    {
      // TODO implement a salt hash for password

      // Steps to build id:
      // execute query to obtain app, role for user_id
      String shadowFile = "";
      try
      {
        shadowFile = Finder.getEnv("YADA.shadow");
      }
      catch (YADAResourceException e)
      {
        l.info("[YADA.shadow] property not set. Using default.");
        shadowFile = YADA_SHADOW;
      }      
        
      String shadowPath = String.format("%s/%s", Finder.getYADALib(), shadowFile);
      String shadow;
      try
      {
        Set<PosixFilePermission> filePerm = null;
        try
        {
          filePerm = Files.getPosixFilePermissions(Paths.get(shadowPath));
        }
        catch (IOException e)
        {
          String msg = "Unable to retrieve permissions of shadow file";
          throw new YADAResourceException(msg, e);
        }
        String permission = PosixFilePermissions.toString(filePerm);
        if(!permission.contentEquals("rw-------"))
        {
          String msg = "Unable to proceed with authorization. Shadow file permissions must match 600";
          throw new YADASecurityException(msg);
        }

        shadow = new String(Files.readAllBytes(Paths.get(shadowPath)), StandardCharsets.UTF_8);
        JSONObject jshadow  = new JSONObject(shadow);
        JSONObject identity = jshadow.getJSONObject("users").getJSONObject(userid);
        String     hash     = identity.getString(YADA_IDENTITY_HASH);
        Verifier   verifier = jargon2Verifier();
        boolean    matches  = verifier.hash(hash).password(pw.getBytes()).verifyEncoded();
        if(!matches)
        {
          String msg = "Unable to proceed with authorization. Check credentials.";
          throw new YADASecurityException(msg);
        }
        JSONArray  grants   = identity.getJSONArray(YADA_IDENTITY_GRANTS); //{"app":app,"keys":["","",""]} 
        JSONArray  keys     = new JSONArray();
        for(int i=0; i<grants.length();i++)
        {
          String    app     = grants.getJSONObject(i).getString(YADA_IDENTITY_APP);
          JSONArray appkeys = grants.getJSONObject(i).getJSONArray(YADA_IDENTITY_KEYS);
          for(int j=0; j<appkeys.length();j++)
          {
            JSONObject kp = new JSONObject();
            kp.put(YADA_IDENTITY_KEY, appkeys.get(j));
            keys.put(kp);
          }
          JSONObject ap = new JSONObject();
          ap.put(YADA_IDENTITY_APP, app).put(YADA_IDENTITY_KEYS, keys);
          aid.put(ap);
        }

      }
      catch (IOException e)
      {
        String msg = "Unable to load credenitial store";
        throw new YADAResourceException(msg, e);
      }

      // app = APP
      // key = ROLE
      // store json { "app" : app, "key" : key } for each row
      // in json array `a`
      // xform json objects to form `grantsObj`:
      // {"app": app, "keys": [{"key":key1},("key":keyN}]}
      // populate `result` object:
      // { "sub" : user_id, "X-CSRF-Token": getSyncToken(),
      // "grants": grantsObj, "iat": java.time.Instant.now().getEpochSecond()

    }
    else
    {
      try (ResultSet rs = YADAUtils.executePreparedStatement(YADA_LOGIN_QUERY, new Object[] { userid, pw });)
      {
        while (rs.next())
        {
          String app = rs.getString(1); // YADA_LOGIN.APP
          al.add(app);
          String key = rs.getString(3); // YADA_LOGIN.ROLE (a key name)
          // a is used to get the form:
          // [{"app":app1,"key":key1},{"app":app1,"kiey":key2},...{"app":appN,"key":keyN}]
          a.put(new JSONObject().put(YADA_IDENTITY_APP, app).put(YADA_IDENTITY_KEY, key));
        }
        ConnectionFactory.releaseResources(rs); // this is redundant due to try-with-resources

        // use unique al entries and get the form:
        // [{"app":app1,"keys":[{"key":key1},{"key":key2},...{"key":keyN}]}]
        if (a.length() > 0)
        {
          // Distill al to applist by removing any duplicate apps
          ArrayList<String> applist = new ArrayList<String>();
          for (String app: al)
          {
            if (!applist.contains(app))
            {
              applist.add(app);
            }
          }
          // assemble identity object
          
          for (int i = 0; i < applist.size(); i++)
          {
            JSONArray keys = new JSONArray();
            for (int ii = 0; ii < a.length(); ii++)
            {
              if (applist.get(i).equals(a.getJSONObject(ii).getString(YADA_IDENTITY_APP).toString()))
              {
                // {YADA_IDENTITY_KEY,
                // a.getJSONObject(i).getString(YADA_IDENTITY_KEY)}
                keys.put(new JSONObject().put(YADA_IDENTITY_KEY, a.getJSONObject(i).getString(YADA_IDENTITY_KEY)));
              }
            }
            aid.put(new JSONObject().put(YADA_IDENTITY_APP, applist.get(i)).put(YADA_IDENTITY_KEYS, keys));
          }          
        }        
      }
      catch (SQLException | YADAConnectionException | YADASQLException e)
      {
        String msg = "Unauthorized.";
        throw new YADASecurityException(msg, e);
      }
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
    // setIdentity(result);
    return result.toString();
  }

  /**
   * @return
   * @since 8.7.6
   */
  public Object obtainIdentity() {
    Object result = getCacheEntry(YADA_IDENTITY_CACHE, (String) this.getToken());
    return result;
  }

  /**
   * Obtain specified GRANT(KEYS) from current identity
   * 
   * @param app
   * @return
   */
  public Object obtainGrant(String app) {
    JSONObject identity = new JSONObject((String) getIdentity());
    JSONArray  identGrants = identity.getJSONArray(YADA_IDENTITY_GRANTS);
    // find the app
    JSONArray keys = new JSONArray();
    for (int i = 0; i < identGrants.length(); i++)
    {
      String identGrantApp = identGrants.getJSONObject(i).getString(YADA_IDENTITY_APP).toString();
      if (app.equals(identGrantApp))
      {
        JSONArray identGrantKeys = identGrants.getJSONObject(i).getJSONArray(YADA_IDENTITY_KEYS);
        for (int ii = 0; ii < identGrantKeys.length(); ii++)
        {
          keys.put(identGrantKeys.getJSONObject(ii).getString(YADA_IDENTITY_KEY));
        }
      }
    }
    return keys;
  }

  /**
   * @return {@code true} if {@link AbstractPostprocessor#getToken()} is set,
   *         otherwise {@code false}
   * @throws YADASecurityException
   * @since 8.7.6
   */
  public boolean hasToken() throws YADASecurityException {
    if (null != this.getToken() && !"".equals(this.getToken()))
    {
      return true;
    }
    return false;
  }

  /**
   * @return {@code true} if {@link #locks} has at least 1 entry, otherwise
   *         {@code false}
   * @since 8.7.6
   */
  public boolean hasLocks() {
    if (getLocks().length() > 0)
    {
      return true;
    }
    return false;
  }

  /**
   * @return {@code true} if {@link #grant} has at least 1 entry, otherwise
   *         {@code false}
   * @since 8.7.6
   */
  public boolean hasGrants() {
    if (((JSONArray) getGrant()).length() > 0)
    {
      return true;
    }
    return false;
  }

  /**
   * @return
   * @since 8.7.6
   */
  public boolean hasIdentity() {
    if (null != getIdentity() && !"".equals(getIdentity()))
    {
      return true;
    }
    return false;
  }

  /**
   * @return {@code true} if {@link #allowList} has at least 1 entry, otherwise
   *         {@code false}
   * @since 8.7.6
   */
  public boolean hasAllowList() {
    if (getAllowList().size() > 0)
    {
      return true;
    }
    return false;
  }

  /**
   * @return {@code true} if {@link #credentials} has at least 1 entry, otherwise
   *         {@code false}
   * @since 8.7.6
   */
  public boolean hasCredentials() {
    if (null != getCredentials() && !"".equals(getCredentials()))
    {
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
   * @param yadaAuthorization the yadaAuthorization to set
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
   * @param resource the resource to set
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
   * @param credentials the credentials to set
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
   * @param grant the grant to set
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
   * @param identity the identity to set
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
   * @param locks the locks to set
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
   * @param app the app to set
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
   * @param result the result to set
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
   * @param syncToken the syncToken to set
   */
  public void setSyncToken(String syncToken) {
    this.syncToken = syncToken;
  }

}

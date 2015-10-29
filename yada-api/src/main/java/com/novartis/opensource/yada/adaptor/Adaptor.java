/**
 * 
 */
package com.novartis.opensource.yada.adaptor;

import org.apache.log4j.Logger;

import com.novartis.opensource.yada.YADAQuery;
import com.novartis.opensource.yada.YADAQueryResult;
import com.novartis.opensource.yada.YADARequest;

/**
 * The abstract Adaptor class is at the root of the package hierarchy. Adaptors
 * serve three purposes:
 * <ol>
 * <li>Conversion of stored queries containing YADA markup into executable
 * code stripped of markup and wrapped or amended as needed</li>
 * <li>Execution of queries</li>
 * <li>Indexing of results into {@link YADAQueryResult} objects, for downstream
 * conversion</li>
 * </ol>
 * Subclasses of Adaptor provide techonology-specific and vendor-specific
 * adaptations of these functions.
 * 
 * @author David Varon
 * @since 0.4.0.0
 */
public abstract class Adaptor
{

	/**
	 * Local logger handle
	 */
	@SuppressWarnings("unused")
	private static Logger l = Logger.getLogger(Adaptor.class);
	/**
	 * Local request configuration (i.e., {@link YADARequest}) object
	 */
	protected YADARequest yadaReq;

	/**
	 * Default constructor
	 */
	public Adaptor()
	{
	}

	/**
	 * Preferred "Service Parameters" constructor
	 * @param yadaReq YADA request configuration
	 */
	public Adaptor(YADARequest yadaReq)
	{
		setServiceParameters(yadaReq);
	}

	/**
	 * This is a stubbed method. Implementation required in non-abstract subclass
	 * 
	 * @param yq
	 *          the {@link YADAQuery} containing the code to execute
	 * @throws YADAAdaptorExecutionException if exception is thrown during query execution 
	 */
	public void execute(YADAQuery yq) throws YADAAdaptorExecutionException
	{
		//
	}

	/**
	 * @param yq
	 *          the {@link YADAQuery} from which to derive the source code and
	 *          other metadata for contruction of an executable query
	 * @return executable code
	 * @throws YADAAdaptorException
	 *           if exception is thrown while conforming code to executable state
	 */
	public String build(YADAQuery yq) throws YADAAdaptorException
	{
		String result = "";
		try
		{
			// placeholder
		} catch (Exception e)
		{
			String msg = "There was an error.";
			throw new YADAAdaptorException(msg, e);
		}
		return result;
	}

	/**
	 * Standard mutator for variable
	 * 
	 * @param yadaReq YADA request configuration
	 */
	protected void setServiceParameters(YADARequest yadaReq)
	{
		this.yadaReq = yadaReq;
	}

	/**
	 * Standard accessor for variable
	 * 
	 * @return YADA request configuration
	 */
	protected YADARequest getServiceParameters()
	{
		return this.yadaReq;
	}
	
	/**
	 * Sets the {@code count} parameter to {@code false}.  Count of SOAP results is currently not supported.
	 * @param yq the query to which to apply the new settings
	 */
	protected void resetCountParameter(YADAQuery yq) {
		//TODO run this through the debugger to check the distinction betwen these 2 instances of yadaReq
		this.yadaReq.setCount(new String[] {"false"});
		yq.replaceParam(YADARequest.PS_COUNT, "false");
	}
}

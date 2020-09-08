/**
 * Copyright 2016 Novartis Institutes for BioMedical Research Inc.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.novartis.opensource.yada.test.pre9;

import org.testng.annotations.Test;

import com.novartis.opensource.yada.Finder;
import com.novartis.opensource.yada.YADAConnectionException;
import com.novartis.opensource.yada.YADAFinderException;
import com.novartis.opensource.yada.YADAQueryConfigurationException;

/**
 * Contains methods for testing query retrieval and query retrieval failure.
 * @author David Varon
 * @since 9.0.0
 */
public class FinderTest {

  /**
   * Tests {@link Finder#getQuery(String)}.  The test is successful if the query {@code YADA test SELECT} is retrieved without error.
   * @throws YADAConnectionException when the connection to the YADA index can't be opened
   * @throws YADAFinderException when the specified query can't be found
   * @throws YADAQueryConfigurationException when the default params are malformed
   */
  @Test (groups = {"core"})
  public void getExistingQuery() throws YADAConnectionException, YADAFinderException, YADAQueryConfigurationException
  {
  	new Finder().getQuery("YADA test SELECT");
  }


  /**
   * Tests {@link Finder#getQuery(String)} with a non-existent query.
   * The test is successful if retrieval the query {@code YADA fake query} causes a {@link YADAFinderException} to be thrown.
   * @throws YADAConnectionException when the connection to the YADA index can't be opened
   * @throws YADAFinderException when the specified query can't be found
   * @throws YADAQueryConfigurationException when the default params are malformed
   */
  @Test (groups = {"core"}, expectedExceptions = YADAFinderException.class)
  public void getUnknownQuery() throws YADAConnectionException, YADAFinderException, YADAQueryConfigurationException
  {
  	new Finder().getQuery("YADA fake query");
  }

}

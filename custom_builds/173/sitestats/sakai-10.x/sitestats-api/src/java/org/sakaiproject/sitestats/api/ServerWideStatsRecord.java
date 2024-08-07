/**
 * $URL: https://svn.rsmart.com/svn/vendor/branches/sakai/rsmart-cle/sitestats/sitestats-api/src/java/org/sakaiproject/sitestats/api/ServerWideStatsRecord.java $
 * $Id: ServerWideStatsRecord.java 20556 2010-04-22 22:16:37Z jbush $
 *
 * Copyright (c) 2006-2009 The Sakai Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *             http://www.opensource.org/licenses/ECL-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.sakaiproject.sitestats.api;


public interface ServerWideStatsRecord {

    public Object get (int index);
    public void add (Object e);

}

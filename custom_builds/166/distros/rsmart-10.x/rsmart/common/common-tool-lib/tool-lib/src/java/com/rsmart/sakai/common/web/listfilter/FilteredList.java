/*
 * Copyright 2008 The rSmart Group
 *
 * The contents of this file are subject to the Mozilla Public License
 * Version 1.1 (the "License"); you may not use this file except in
 * compliance with the License. You may obtain a copy of the License at
 * http://www.mozilla.org/MPL/
 *
 * Software distributed under the License is distributed on an "AS IS"
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See the
 * License for the specific language governing rights and limitations
 * under the License.
 *
 * Contributor(s): jbush
 */
package com.rsmart.sakai.common.web.listfilter;

import org.sakaiproject.javax.Filter;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;



/**
 *
 */
public class FilteredList extends ArrayList {

   public FilteredList(List list, Filter filter) {
      super();
      if (filter != null) {
         for (Iterator i = list.iterator(); i.hasNext();) {
            Object object = i.next();
            if (filter.accept(object))
               add(object);
         }
      } else {
         addAll(list);
      }
   }
}

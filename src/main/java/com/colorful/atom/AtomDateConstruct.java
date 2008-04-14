/*
Atomsphere - an atom feed library.
Copyright (C) 2006 William R. Brown.

This program is free software; you can redistribute it and/or
modify it under the terms of the GNU General Public License
as published by the Free Software Foundation; either version 2
of the License, or (at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with this program; if not, write to the Free Software
Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
/* Change History:
 *  2008-03-16 wbrown - Introduced to share between published and updated.
 */
package com.colorful.atom;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

class AtomDateConstruct {

	private final Date dateTime;
    
    /**
     * 
     * @param updated the date formatted to [RFC3339]
     */
    public AtomDateConstruct(Date dateTime){
    	if(dateTime == null){
    		this.dateTime = null;
    	}else{
    		this.dateTime = new Date(dateTime.getTime());
    	}
    }
    
    /**
     * 
     * @return the date timestamp for this element.
     */
    protected Date getDateTime(){
    	return (dateTime == null)? null: new Date(dateTime.getTime());
    }
    
    /**
     * 
     * @return the string formated version of the time
     * 	for example 2006-04-28T12:50:43.337-05:00
     */
    public String getText() {
    	if(dateTime == null){
    		return null;
    	}
    	//example 2006-04-28T12:50:43.337-05:00
    	final String timeZoneOffset;
    	TimeZone timeZone = TimeZone.getDefault();
        int hours = (((timeZone.getRawOffset()/1000)/60)/60);
        if(hours >= 0){
            timeZoneOffset = TimeZone.getTimeZone("GMT"+"+"+hours).getID().substring(3);
        }else{
            timeZoneOffset = TimeZone.getTimeZone("GMT"+"-"+Math.abs(hours)).getID().substring(3);
        }
        return new SimpleDateFormat("yyyy-MM-dd\'T\'HH:mm:ss.SS\'"+timeZoneOffset+"\'").format(dateTime);
    }
}
/*
    Utils: MutableInt provides class that should lead to more efficient storage for larger numbers of Integers
  
    Adapted from: 
    https://github.com/apache/commons-lang/blob/master/src/main/java/org/apache/commons/lang3/mutable/MutableInt.java
    License: https://www.apache.org/licenses/LICENSE-2.0
    
    ====
    
    Portions Copyright (C) 2018 National Gallery of Art Washington DC
    Developers: NGA Contractors

	This library is free software; you can redistribute it and/or
	modify it under the terms of the GNU Lesser General Public
	License as published by the Free Software Foundation; either
	version 2.1 of the License, or (at your option) any later version.

	This library is distributed in the hope that it will be useful,
	but WITHOUT ANY WARRANTY; without even the implied warranty of
	MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
	Lesser General Public License for more details.

	You should have received a copy of the GNU Lesser General Public
	License along with this library; if not, write to the Free Software
	Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
*/
package gov.nga.utils;

public class MutableInt implements Comparable<MutableInt> {

	private int value = 0;

	public MutableInt(int value) {
		this.value = value;
	}

	public void inc () { 
		++value;
	}

	public int  get () { 
		return value; 
	}

	public int compareTo(MutableInt o) {
		return Integer.valueOf(get()).compareTo(Integer.valueOf(o.get()));
	}

}

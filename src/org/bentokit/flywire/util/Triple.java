/*
    This file is part of Bentokit Flywire.

    Bentokit Flywire is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    Bentokit Flywire is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with Bentokit Flywire.  If not, see <http://www.gnu.org/licenses/>.
*/
package org.bentokit.flywire.util;

public class Triple<T1, T2, T3> {
        public T1 first;
        public T2 second;
        public T3 third;

        public Triple() {
                this.first = null;
                this.second = null;
                this.third = null;
        }
                
        public Triple(T1 first, T2 second, T3 third) {
                this.first = first;
                this.second = second;
                this.third = third;
        }
                
        public boolean equals(Triple<T1,T2,T3> t2) {
            if (!this.first.equals(t2.first)) return(false);
            if (!this.second.equals(t2.second)) return(false);
            if (!this.third.equals(t2.third)) return(false);
            return(true);
        }

        public int hashCode() {
            return(this.first.hashCode()
                   +this.second.hashCode()
                   +this.third.hashCode());
        }       
}


package org.omri.radioservice;

import java.util.ArrayList;

/**
 * Copyright (C) 2020 realzoulou
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * 
 * Interface for Service Following
 *   
 * @author realzoulou
 */
public interface RadioServiceFollowingListener extends RadioServiceListener {

	/**
	 * Informs about new list of {@link RadioService}s that can be used to follow the current service
	 * @param radioServices ordered list of {@link RadioService}s
	 */
	public void newServiceFollowingRadioServices(ArrayList<RadioService> radioServices);
}

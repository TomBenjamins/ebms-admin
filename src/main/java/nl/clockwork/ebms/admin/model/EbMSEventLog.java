/**
 * Copyright 2013 Clockwork
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package nl.clockwork.ebms.admin.model;

import java.util.Date;

import org.apache.wicket.util.io.IClusterable;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import nl.clockwork.ebms.event.processor.EbMSEventStatus;

@Builder
@Data
@AllArgsConstructor
@RequiredArgsConstructor
public class EbMSEventLog implements IClusterable
{
	private static final long serialVersionUID = 1L;
	EbMSMessage message;
	@NonNull
	Date timestamp;
	@NonNull
	String uri;
	@NonNull
	EbMSEventStatus status;
	String errorMessage;
}

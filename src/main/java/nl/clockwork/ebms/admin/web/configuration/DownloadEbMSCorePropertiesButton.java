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
package nl.clockwork.ebms.admin.web.configuration;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;

import nl.clockwork.ebms.admin.web.configuration.Constants.PropertiesType;
import nl.clockwork.ebms.admin.web.configuration.EbMSCorePropertiesPage.EbMSCorePropertiesFormModel;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.wicket.markup.html.form.Button;
import org.apache.wicket.model.ResourceModel;
import org.apache.wicket.request.IRequestCycle;
import org.apache.wicket.request.handler.resource.ResourceStreamRequestHandler;
import org.apache.wicket.request.resource.ContentDisposition;
import org.apache.wicket.util.lang.Bytes;
import org.apache.wicket.util.resource.AbstractResourceStream;
import org.apache.wicket.util.resource.IResourceStream;
import org.apache.wicket.util.resource.ResourceStreamNotFoundException;

public class DownloadEbMSCorePropertiesButton extends Button
{
	private static final long serialVersionUID = 1L;
	protected transient Log logger = LogFactory.getLog(this.getClass());
	private EbMSCorePropertiesFormModel ebMSCorePropertiesFormModel;

	public DownloadEbMSCorePropertiesButton(String id, ResourceModel resourceModel, EbMSCorePropertiesFormModel ebMSCorePropertiesFormModel)
	{
		super(id,resourceModel);
		this.ebMSCorePropertiesFormModel = ebMSCorePropertiesFormModel;
	}

	@Override
	public void onSubmit()
	{
		try
		{
			final StringWriter writer = new StringWriter();
			new EbMSCorePropertiesWriter(writer).write(ebMSCorePropertiesFormModel);
			IResourceStream resourceStream = new AbstractResourceStream()
			{
				private static final long serialVersionUID = 1L;

				@Override
				public String getContentType()
				{
					return "plain/text";
				}
				
				@Override
				public Bytes length()
				{
					return Bytes.bytes(writer.getBuffer().length());
				}
				
				@Override
				public InputStream getInputStream() throws ResourceStreamNotFoundException
				{
					return new ByteArrayInputStream(writer.toString().getBytes());
				}
				
				@Override
				public void close() throws IOException
				{
				}
			}; 

			getRequestCycle().scheduleRequestHandlerAfterCurrent(
				new ResourceStreamRequestHandler(resourceStream)
				{
					@Override
					public void respond(IRequestCycle requestCycle)
					{
						super.respond(requestCycle);
					}
				}
				.setFileName(PropertiesType.EBMS_CORE.getPropertiesFile())
				.setContentDisposition(ContentDisposition.ATTACHMENT)
			);
		}
		catch (IOException e)
		{
			logger.error("",e);
			error(e.getMessage());
		}
	}

}
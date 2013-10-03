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
package nl.clockwork.ebms.admin.web.message;

import java.util.Iterator;

import nl.clockwork.ebms.admin.dao.EbMSDAO;
import nl.clockwork.ebms.admin.model.EbMSMessage;

import org.apache.wicket.markup.repeater.data.IDataProvider;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;

public class MessageDataProvider implements IDataProvider<EbMSMessage>
{
	private static final long serialVersionUID = 1L;
	private EbMSDAO ebMSDAO;

	public MessageDataProvider(EbMSDAO ebMSDAO)
	{
		this.ebMSDAO = ebMSDAO;
	}
	
	@Override
	public Iterator<? extends EbMSMessage> iterator(long first, long count)
	{
		return ebMSDAO.getMessages(first,count).iterator();
	}

	@Override
	public IModel<EbMSMessage> model(EbMSMessage message)
	{
		final String messageId = message.getMessageId();
		final int messageNr = message.getMessageNr();
		return new LoadableDetachableModel<EbMSMessage>()
		{
			private static final long serialVersionUID = 1L;

			@Override
			protected EbMSMessage load()
			{
				return ebMSDAO.getMessage(messageId,messageNr);
			}
		};
	}

	@Override
	public long size()
	{
		return (int)ebMSDAO.getMessageCount();
	}

	@Override
	public void detach()
	{
	}

}

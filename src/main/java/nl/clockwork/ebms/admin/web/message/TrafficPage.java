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

import java.util.Date;

import nl.clockwork.ebms.Constants.EbMSMessageStatus;
import nl.clockwork.ebms.admin.Constants;
import nl.clockwork.ebms.admin.dao.EbMSDAO;
import nl.clockwork.ebms.admin.model.EbMSMessage;
import nl.clockwork.ebms.admin.web.BasePage;

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.ajax.markup.html.navigation.paging.AjaxPagingNavigator;
import org.apache.wicket.datetime.markup.html.basic.DateLabel;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.repeater.Item;
import org.apache.wicket.markup.repeater.data.DataView;
import org.apache.wicket.model.AbstractReadOnlyModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.spring.injection.annot.SpringBean;

public class TrafficPage extends BasePage
{
	private static final long serialVersionUID = 1L;
	@SpringBean(name="ebMSDAO")
	private EbMSDAO ebMSDAO;
	@SpringBean(name="maxItemsPerPage")
	private Integer maxItemsPerPage;
	private EbMSMessageFilter filter;

	public TrafficPage()
	{
		this(new EbMSMessageFilter());
	}

	public TrafficPage(EbMSMessageFilter filter)
	{
		this(filter,null);
	}

	public TrafficPage(EbMSMessageFilter filter, final WebPage responsePage)
	{
		this.filter = filter;
		this.filter.setMessageNr(0);
		this.filter.setServiceMessage(false);
		WebMarkupContainer container = new WebMarkupContainer("container");
		container.setOutputMarkupId(true);

		DataView<EbMSMessage> messages = new DataView<EbMSMessage>("messages",new MessageDataProvider(ebMSDAO,this.filter))
		{
			private static final long serialVersionUID = 1L;

			@Override
			protected void populateItem(final Item<EbMSMessage> item)
			{
				final EbMSMessage message = item.getModelObject();
				Link<Void> link = new Link<Void>("view")
				{
					private static final long serialVersionUID = 1L;

					@Override
					public void onClick()
					{
						//setResponsePage(new MessagePage(ebMSDAO.getMessage(message.getMessageId(),message.getMessageNr()),MessagesPage.this));
						setResponsePage(new MessagePage(message,TrafficPage.this));
					}
				};
				link.add(new Label("messageId",message.getMessageId()));
				item.add(link);
				link = new Link<Void>("filterConversationId")
				{
					private static final long serialVersionUID = 1L;

					@Override
					public void onClick()
					{
						EbMSMessageFilter filter = new EbMSMessageFilter();
						filter.setConversationId(message.getConversationId());
						setResponsePage(new TrafficPage(filter,TrafficPage.this));
					}
				};
				link.add(new Label("conversationId",message.getConversationId()));
				link.setEnabled(TrafficPage.this.filter.getConversationId() == null);
				item.add(link);
				item.add(DateLabel.forDatePattern("timestamp",new Model<Date>(message.getTimestamp()),Constants.DATETIME_FORMAT));
				item.add(new Label("cpaId",message.getCpaId()));
				item.add(new Label("fromRole",message.getFromRole()));
				item.add(new Label("toRole",message.getToRole()));
				item.add(new Label("service",message.getService()));
				item.add(new Label("action",message.getAction()));
				item.add(new Label("status",message.getStatus() == null ? "PENDING" : message.getStatus()).add(AttributeModifier.replace("class",Model.of(getHtmlClass(message.getStatus())))));
				item.add(DateLabel.forDatePattern("statusTime",new Model<Date>(message.getStatusTime()),Constants.DATETIME_FORMAT));
				item.add(AttributeModifier.replace("class",new AbstractReadOnlyModel<String>()
				{
					private static final long serialVersionUID = 1L;
				
					@Override
					public String getObject()
					{
						return (item.getIndex() % 2 == 0) ? "even" : "odd";
					}
				}));
			}
		};
		messages.setOutputMarkupId(true);
		messages.setItemsPerPage(maxItemsPerPage);

		container.add(messages);
		add(container);
		add(new AjaxPagingNavigator("navigator",messages));
		add(new Link<Object>("back")
		{
			private static final long serialVersionUID = 1L;

			@Override
			public void onClick()
			{
				setResponsePage(responsePage);
			}
		}.setVisible(responsePage != null));
		add(new DownloadEbMSMessagesCSVLink("download",ebMSDAO,filter));
	}

	private String getHtmlClass(EbMSMessageStatus ebMSMessageStatus)
	{
		if (EbMSMessageStatus.PROCESSED.equals(ebMSMessageStatus) || EbMSMessageStatus.FORWARDED.equals(ebMSMessageStatus) || EbMSMessageStatus.ACKNOWLEDGED.equals(ebMSMessageStatus))
			return "ok";
		if (ebMSMessageStatus == null || EbMSMessageStatus.RECEIVED.equals(ebMSMessageStatus))
			return "warn";
		if (EbMSMessageStatus.UNAUTHORIZED.equals(ebMSMessageStatus) || EbMSMessageStatus.NOT_RECOGNIZED.equals(ebMSMessageStatus) || EbMSMessageStatus.FAILED.equals(ebMSMessageStatus) || EbMSMessageStatus.DELIVERY_FAILED.equals(ebMSMessageStatus) || EbMSMessageStatus.NOT_ACKNOWLEDGED.equals(ebMSMessageStatus))
			return "error";
		return null;
	}

	@Override
	public String getPageTitle()
	{
		return getLocalizer().getString("messages",this);
	}
}

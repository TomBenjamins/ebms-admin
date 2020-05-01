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
package nl.clockwork.ebms.admin.web;

import java.net.URLConnection;

import org.apache.commons.lang3.StringUtils;

import lombok.val;
import nl.clockwork.ebms.EbMSMessageStatus;

public class Utils
{
	public static String getResourceString(Class<?> clazz, String propertyName)
	{
		val loaders = WicketApplication.get().getResourceSettings().getStringResourceLoaders();
		return loaders.stream().map(l -> l.loadStringResource(clazz,propertyName,null,null,null)).filter(s -> StringUtils.isNotBlank(s)).findFirst().orElse(propertyName);
	}

	public static String getContentType(String pathInfo)
	{
		val result = URLConnection.guessContentTypeFromName(pathInfo);
		//val result = new MimetypesFileTypeMap().getContentType(pathInfo);
		//val result = URLConnection.getFileNameMap().getContentTypeFor(pathInfo);
		return result == null ? "application/octet-stream" : result;
	}

	public static String getFileExtension(String contentType)
	{
		if (StringUtils.isEmpty(contentType))
			return "";
		return "." + (contentType.contains("text") ? "txt" : contentType.split("/")[1]);
	}

	public static String getTableCellCssClass(EbMSMessageStatus ebMSMessageStatus)
	{
		if (EbMSMessageStatus.PROCESSED.equals(ebMSMessageStatus) || EbMSMessageStatus.FORWARDED.equals(ebMSMessageStatus) || EbMSMessageStatus.DELIVERED.equals(ebMSMessageStatus))
			return "text-success";
		if (EbMSMessageStatus.RECEIVED.equals(ebMSMessageStatus) || EbMSMessageStatus.SENDING.equals(ebMSMessageStatus))
			return "text-warning";
		if (EbMSMessageStatus.UNAUTHORIZED.equals(ebMSMessageStatus) || EbMSMessageStatus.NOT_RECOGNIZED.equals(ebMSMessageStatus) || EbMSMessageStatus.FAILED.equals(ebMSMessageStatus) || EbMSMessageStatus.DELIVERY_FAILED.equals(ebMSMessageStatus) || EbMSMessageStatus.EXPIRED.equals(ebMSMessageStatus))
			return "text-danger";
		return null;
	}

	public static String getTableRowCssClass(EbMSMessageStatus ebMSMessageStatus)
	{
		if (EbMSMessageStatus.PROCESSED.equals(ebMSMessageStatus) || EbMSMessageStatus.FORWARDED.equals(ebMSMessageStatus) || EbMSMessageStatus.DELIVERED.equals(ebMSMessageStatus))
			return "success";
		if (EbMSMessageStatus.RECEIVED.equals(ebMSMessageStatus) || EbMSMessageStatus.SENDING.equals(ebMSMessageStatus))
			return "warning";
		if (EbMSMessageStatus.UNAUTHORIZED.equals(ebMSMessageStatus) || EbMSMessageStatus.NOT_RECOGNIZED.equals(ebMSMessageStatus) || EbMSMessageStatus.FAILED.equals(ebMSMessageStatus) || EbMSMessageStatus.DELIVERY_FAILED.equals(ebMSMessageStatus) || EbMSMessageStatus.EXPIRED.equals(ebMSMessageStatus))
			return "danger";
		return null;
	}

	public static String getErrorList(String content)
	{
		return content.replaceFirst("(?ms)^.*(<[^<>]*:?ErrorList.*ErrorList>).*$","$1");
	}

}

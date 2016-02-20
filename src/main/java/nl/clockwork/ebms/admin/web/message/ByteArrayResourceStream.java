package nl.clockwork.ebms.admin.web.message;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

import org.apache.wicket.util.lang.Bytes;
import org.apache.wicket.util.resource.AbstractResourceStream;
import org.apache.wicket.util.resource.ResourceStreamNotFoundException;

public class ByteArrayResourceStream extends AbstractResourceStream
{
	private static final long serialVersionUID = 1L;
	private ByteArrayOutputStream stream;
	private String contentType;
	
	public ByteArrayResourceStream(ByteArrayOutputStream stream, String contentType)
	{
		this.stream = stream;
		this.contentType = contentType;
	}

	@Override
	public String getContentType()
	{
		return contentType;
	}
	
	@Override
	public Bytes length()
	{
		return Bytes.bytes(stream.size());
	}
	
	@Override
	public InputStream getInputStream() throws ResourceStreamNotFoundException
	{
		return new ByteArrayInputStream(stream.toByteArray());
	}
	
	@Override
	public void close() throws IOException
	{
	}
}

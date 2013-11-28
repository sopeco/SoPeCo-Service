
import static org.junit.Assert.*;

import java.io.IOException;

import javax.ws.rs.core.MediaType;

import org.junit.Test;

import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.test.framework.JerseyTest;

public class Tester extends JerseyTest {

	public static final String PACKAGE_NAME = "org.sopeco.service";
	
	public Tester() {
		super("org.sopeco.service");
    }
	
    @Test
    public void test() throws IllegalArgumentException, IOException {
        WebResource webResource = resource().path("login");
        assertEquals("6A1337B7",webResource.accept(MediaType.TEXT_PLAIN).get(String.class));
    }
	
}

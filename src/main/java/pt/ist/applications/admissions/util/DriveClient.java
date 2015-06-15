package pt.ist.applications.admissions.util;

import java.io.IOException;
import java.io.InputStream;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.Invocation.Builder;
import javax.ws.rs.core.MediaType;

import org.apache.commons.io.IOUtils;
import org.fenixedu.bennu.ApplicationsAdmissionsConfiguration;
import org.fenixedu.bennu.ApplicationsAdmissionsConfiguration.ConfigurationProperties;
import org.glassfish.jersey.media.multipart.FormDataMultiPart;
import org.glassfish.jersey.media.multipart.MultiPart;
import org.glassfish.jersey.media.multipart.MultiPartFeature;
import org.glassfish.jersey.media.multipart.file.StreamDataBodyPart;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public class DriveClient {

    private static final Client CLIENT = ClientBuilder.newClient();
    static {
        CLIENT.register(MultiPartFeature.class);
    }

    private static ConfigurationProperties getConfig() {
        return ApplicationsAdmissionsConfiguration.getConfiguration();
    }

    private static String getAccessToken() {
        final String refresh_token = getConfig().refreshToken();
        final String path = "/api/docs/oauth/" + getConfig().clientAppId() + "/" + getConfig().clientAppUser();
        final String post2 = target(path, refresh_token).get(String.class);
        final JsonObject o2 = new JsonParser().parse(post2).getAsJsonObject();
        return o2.get("access_token").getAsString();
    }

    public static void upload(final String directory, final String filename, final InputStream inputStream,
            final String contentType) {
        final String[] mediaType = contentType.split("/");
        final StreamDataBodyPart streamDataBodyPart =
                new StreamDataBodyPart("file", inputStream, filename, new MediaType(mediaType[0], mediaType[1]));
        try (final FormDataMultiPart formDataMultiPart = new FormDataMultiPart()) {
            final MultiPart entity = formDataMultiPart.bodyPart(streamDataBodyPart);
            final String path = "/api/docs/directory/" + directory;
            target(path).post(Entity.entity(entity, MediaType.MULTIPART_FORM_DATA_TYPE), String.class);
        } catch (final IOException e) {
            throw new Error(e);
        }
    }

    public static void download(final String id, final HttpServletResponse response) throws IOException {
        final InputStream inputStream = target("/api/docs/file/" + id + "/download").get(InputStream.class);
        final ServletOutputStream outputStream = response.getOutputStream();
        IOUtils.copy(inputStream, outputStream);
        outputStream.close();
        inputStream.close();
    }

    public static String createDirectory(final String parent, final String name) {
        final String putDir =
                target("/api/docs/directory/" + parent).put(
                        Entity.entity("{name: \"" + name + "\"}", MediaType.APPLICATION_JSON), String.class);
        final JsonObject o3 = new JsonParser().parse(putDir).getAsJsonObject();
        return o3.get("id").getAsString();
    }

    public static String createDirectory(final String name) {
        return createDirectory(getConfig().contestDir(), name);
    }

    public static JsonArray listDirectory(final String directory) {
        final String post3 = target("/api/docs/directory/" + directory).get(String.class);
        final JsonObject o3 = new JsonParser().parse(post3).getAsJsonObject();
        System.out.println(o3.toString());
        return o3.get("items").getAsJsonArray();
    }

    public static void deleteDirectory(final String directory) {
        target("/api/docs/directory/" + directory).delete();
    }

    private static Builder target(final String path) {
        return target(path, getAccessToken());
    }

    private static Builder target(final String path, final String token) {
        return CLIENT.target(getConfig().driveUrl() + path).queryParam("access_token", token).request();
    }

}

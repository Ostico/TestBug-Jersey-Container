package org.example;

import jakarta.servlet.ServletInputStream;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.eclipse.jetty.ee10.servlet.ServletContextHandler;
import org.eclipse.jetty.ee10.servlet.ServletHolder;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.servlet.ServletContainer;

import java.io.InputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * @author Domenico Lupinetti <ostico@gmail.com> on 10/10/24
 */
public class Main {

    public static void main( String[] args ) throws Exception {

        Server server = Main.newServer( 8080 );
        server.start();

//        HttpPostCall callerClient = new HttpPostCall();
//        callerClient.scheduleHttpCall();

        server.join();
    }


    public static Server newServer( int port ) {
        Server server = new Server();

        ServerConnector connector = new ServerConnector( server );
        connector.setPort( port );
        server.addConnector( connector );


        ResourceConfig resConfig = new ResourceConfig();
        resConfig.register( new Analyze() );

        ServletContainer jerseyServletContainer = new ServletContainer( resConfig );
        ServletHolder    jettyServletHolder     = new ServletHolder( jerseyServletContainer );

        ServletContextHandler context = new ServletContextHandler( ServletContextHandler.NO_SESSIONS );
        context.setContextPath( "/" );

        context.addServlet( jettyServletHolder, "/api/*" );

        server.setHandler( context );

        return server;
    }

    @Path("/v1")
    @Produces(MediaType.APPLICATION_JSON)
    public static class Analyze {

        @Context
        private HttpServletRequest request;

        @POST
        @Path("/analyze")
        @Produces(MediaType.APPLICATION_JSON)
        @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
        public Response analyze() {

            try {

                System.out.println( "Request received" );

                ServletInputStream inputStream = request.getInputStream();

                byte[] content = inputStream.readAllBytes();

                String jsonString = new String( content, StandardCharsets.UTF_8 );

                System.out.println( "First char:" + jsonString.charAt( 0 ) );

                if( content[ 0 ] != 91 /* character [ */ ){ // https://www.ascii-code.com/91
                    throw new Exception( "Oops" );
                }

                return Response.ok( "{\"status\":\"OK\"}" ).build();

            } catch ( Exception e ) {
                System.out.println( "ERROR" );
                return Response.serverError().build();
            }

        }


    }


    public static class HttpPostCall {

        public void scheduleHttpCall() {

            ScheduledExecutorService scheduler = Executors.newScheduledThreadPool( 1 );
            // Define the Runnable task, start in 5 seconds and every 5 seconds
            scheduler.scheduleAtFixedRate( this::performPostRequest, 5, 3, TimeUnit.SECONDS );

        }

        public void performPostRequest() {


            // Create the HttpClient
            HttpClient httpClient = HttpClient.newBuilder()
                                              .version( HttpClient.Version.HTTP_1_1 )
                                              .build();


            // Define the JSON payload
            try ( InputStream is = getClass().getClassLoader().getResourceAsStream( "json_test.json" ) ) {

                assert is != null;
                final String json = new String( is.readAllBytes(), StandardCharsets.UTF_8 );
                // Create the POST request
                HttpRequest request = HttpRequest.newBuilder()
                                                 .uri( new URI( "http://localhost:8080/api/v1/analyze" ) )
                                                 .header( "Content-Type", "application/x-www-form-urlencoded" )
                                                 .POST( HttpRequest.BodyPublishers.ofString( json, StandardCharsets.UTF_8 ) )
                                                 .build();

                // Send the request and get the response
                HttpResponse<String> response = httpClient.send( request, HttpResponse.BodyHandlers.ofString() );

                // Print the response status code and body
                System.out.println( "Response code: " + response.statusCode() );
                System.out.println( "Response body: " + response.body() );


            } catch ( Exception e ) {
                e.printStackTrace();
            }

        }

    }


}


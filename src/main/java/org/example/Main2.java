package org.example;

import jakarta.servlet.ServletInputStream;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.eclipse.jetty.ee10.servlet.ServletContextHandler;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

/**
 * @author Domenico Lupinetti <ostico@gmail.com> on 10/10/24
 */
public class Main2 {

    public static void main( String[] args ) throws Exception {
        Server server = Main2.newServer( 8080 );
        server.start();
        server.join();
    }


    public static Server newServer( int port ) {
        Server server = new Server();

        ServerConnector connector = new ServerConnector( server );
        connector.setPort( port );
        server.addConnector( connector );

        ServletContextHandler context = new ServletContextHandler( ServletContextHandler.NO_SESSIONS );
        context.setContextPath( "/" );

        context.addServlet( Analyze.class, "/api/v1/analyze" );

        server.setHandler( context );

        return server;
    }

    public static class Analyze extends HttpServlet {

        protected void doPost( HttpServletRequest req, HttpServletResponse resp ) {

            try {


                System.out.println( "Request received" );

                ServletInputStream inputStream = req.getInputStream();

                byte[] content = inputStream.readAllBytes();

                String jsonString = new String( content, StandardCharsets.UTF_8 );

                System.out.println( "First char:" + jsonString.charAt( 0 ) );

                if( content[ 0 ] != 91 ){
                    throw new Exception( "Oops" );
                }

                resp.setCharacterEncoding( "utf-8" );
                resp.setContentType( "application/json" );
                resp.getWriter().printf( "{\"status\":\"OK\"}" );

            } catch ( Exception e ) {
                System.out.println( "ERROR" );
                try {
                    resp.sendError( HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "First byte is lacking." );
                } catch ( IOException ex ) {
                    System.out.println( ex.getMessage() );
                }
            }

        }


    }

}
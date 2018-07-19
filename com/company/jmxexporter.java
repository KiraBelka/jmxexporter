package com.company;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.util.Set;
import javax.management.remote.JMXConnector;
import javax.management.remote.JMXConnectorFactory;
import javax.management.remote.JMXServiceURL;
import javax.management.MBeanServerConnection;
import javax.management.MBeanServerInvocationHandler;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;
import javax.management.MBeanInfo;
import javax.management.openmbean.CompositeData;
import java.util.Iterator;
import javax.management.MBeanAttributeInfo;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

public class jmxexporter {
    static public String jmxrmistring;
    static public String jmxport;
    static public String jmxhost;
    public static void main(String[] args) throws Exception {
        if (args.length!=2) {
            System.out.print("Неверное число аргументов! укажите только --port и --rmiurl");
            System.exit(0);
        }
        if (args[0].contains("--port=") == false && args[1].contains("--port=") == false)  {
            System.out.print("Укажите --port");
            System.exit(0);
        }
        if (args[0].contains("--rmiurl=") == false && args[1].contains("--rmiurl=") == false)  {
            System.out.print("Укажите --rmiurl");
            System.exit(0);
        }

        Integer ListenPort = Integer.parseInt(args[0].split("=")[1]);
        jmxrmistring = args[1].split("=")[1];
        jmxhost = jmxrmistring.split(":")[0];
        //System.out.print(jmxhost);
        jmxport = jmxrmistring.split(":")[1];
        //System.out.print(jmxport);
        HttpServer server = HttpServer.create(new InetSocketAddress(ListenPort), 0);
        server.createContext("/metrics", new MyHandler());
        server.setExecutor(null); // creates a default executor
        server.start();
    }

    static class MyHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange t) throws IOException {
            String outputline = "";
            String myjmxhost = jmxhost;
            //System.out.print(myjmxhost);
            String myjmxport = jmxport;
            JMXServiceURL url = new JMXServiceURL("service:jmx:rmi:///jndi/rmi://" + myjmxhost + ":" + myjmxport + "/jmxrmi");
            //System.out.print(url);
            JMXConnector conn = JMXConnectorFactory.connect(url);
            MBeanServerConnection mBeanServer = conn.getMBeanServerConnection();
            try {
               ObjectName jmxmbeanlimit = ObjectName.getInstance("*");
                System.out.println(jmxmbeanlimit);

            Set<ObjectName> mbeans = mBeanServer.queryNames(jmxmbeanlimit,null);
            System.out.println(mbeans);
           Iterator<ObjectName> jmxit = mbeans.iterator();
            while(jmxit.hasNext()) {
                //   System.out.println(jmxit.next());
                try {
                    ObjectName name = jmxit.next();
                    MBeanInfo info = mBeanServer.getMBeanInfo(name);
                    MBeanAttributeInfo[] attrInfo = info.getAttributes();
                    //System.out.print(attrInfo);
                    for (int i = 0; i < attrInfo.length; i++) {

                        //     System.out.println("         Attribute: " + attrInfo[i].getName() +

                        //           "   of Type : " + attrInfo[i].getType());
                        if (attrInfo[i].getType().equals("javax.management.openmbean.CompositeData"))  {
                            //System.out.print( name +"." + attrInfo[i].getName() + " " + mBeanServer.getAttribute(name, attrInfo[i].getName()) + " {" + attrInfo[i].getType() +"}\n");
                            //outputline = outputline + name + "." + attrInfo[i].getName() + " " + mBeanServer.getAttribute(name, attrInfo[i].getName()) + " {" + attrInfo[i].getType() + "}\n";
                            CompositeData stecd = (CompositeData) mBeanServer.getAttribute(name, attrInfo[i].getName());
                            String [] compdata =(stecd.toString().split("contents=")[1].replaceAll("[\\)\\}\\{]","").replaceAll(" ","").replaceAll("="," ").split(","));
                            for(String val: compdata) {
                              //  System.out.println(outputline + name + "." + val);
                                outputline = outputline + name + "." + val + "\n";
                            }
                        }

                                     if (attrInfo[i].getType().equals("int") || attrInfo[i].getType().equals("long") ||  attrInfo[i].getType().equals("float") || attrInfo[i].getType().equals("java.lang.Long") || attrInfo[i].getType().equals("java.lang.Integer")) {
                            //System.out.print( name +"." + attrInfo[i].getName() + " " + mBeanServer.getAttribute(name, attrInfo[i].getName()) + " {" + attrInfo[i].getType() +"}\n");
                            outputline = outputline + name + "." + attrInfo[i].getName() + " " + mBeanServer.getAttribute(name, attrInfo[i].getName()) + " {" + attrInfo[i].getType() + "}\n";
                        }
                    }

                } catch (Exception e) {
                    System.out.print(e.getStackTrace());
                    //   System.exit(0);
                }
            }
            }catch (Exception e) {
                    System.out.print(e.getStackTrace());
                    return ;
                    //  System.exit(0);
                }

          //  }
            conn.close();
           // Integer megacount = mBeanServer.getMBeanCount();
            //System.out.print(megacount);
            String response = outputline;
            t.sendResponseHeaders(200, response.length());
            OutputStream os = t.getResponseBody();
            os.write(response.getBytes());
            os.close();


    }

    }

}

@Grab("org.json:json:20090211")
import org.json.*;
import static groovy.xml.XmlUtil.*;

jsonToHtml = { s ->
    JSONObject j = new JSONObject(s);
    println "<ul>"
    j.keys().toList().each() { k ->
        if(!k.contains("AccessKey") && !k.contains("Token")) {
            println "<li><b>$k</b>:${j.getString(k)}</li>";
        } else {
        	
        	println "<li><b>$k</b>:${j.getString(k).replaceAll('.', '*')}</li>";
        }
    }
    println "</ul>"
}



process = { u, index ->
    print "<ul>";
    def lines = new URL(u).getText().split("\n");
    lines.each() { line ->
        if(line.endsWith("/")) {
            println "<li><b>$line</b>";
            process("$u$line", index+1);
            println "</li>";
        } else {
            print "<li><b>";
            if(line.indexOf('=')==-1) {
                String value = new URL("$u$line/").getText();
                if(value.startsWith("{") && value.endsWith("}")) {
                    print "$line</b>";
                    jsonToHtml(value);
                } else {
                    if(value.startsWith("<?xml")) {
                        value = escapeXml(value);
                    }
                    print "$line</b>:$value";
                }
            } else {
                String[] kv = line.split("=");
                print "${kv[0]}</b>:${kv[1]}"
            }
            println "</li>"
        }
    }
    print "</ul>";
}
long start = System.currentTimeMillis();
println "<html><body>";
process("http://169.254.169.254/latest/meta-data/", 0);
long elapsed = System.currentTimeMillis()-start;
println "<hr><p>Elapsed: $elapsed ms.</p>";
println "</body></html>";
return null;

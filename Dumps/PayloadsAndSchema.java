/*
import org.everit.json.schema.Schema;
import org.everit.json.schema.ValidationException;
import org.json.JSONObject;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import java.util.ArrayList;

// https://www.baeldung.com/java-org-json
// this feature is only for post and put method
// for the rest it will be ignored!

@Component
@Scope("prototype")
public class PayloadsAndSchema {

    private final ArrayList<POSTData> postData ;
    private int payLoadCount;

    public PayloadsAndSchema(){
        postData = new ArrayList <>();
        payLoadCount= 0;
    }

    public synchronized int addPayload(int key,boolean mode, JSONObject object){
        // this whole operation is made synchronized to make sure the addition is atomic.
        // otherwise inconsistency can occur!
        if(key>postData.size()){
            postData.add(new POSTData());
        }
        int ret=++payLoadCount;
        try {
            PayloadResponse payload = new PayloadResponse(ret, mode, object);
            postData.get(key - 1).addPayload(payload);
        }catch(IllegalArgumentException | ValidationException e){
            payLoadCount--;
            throw e;
        }
        return ret;
    }

    public int deletePayload(int key, JSONObject object) throws IllegalArgumentException {
        if(key>postData.size()) throw new IllegalArgumentException("No Mock Query Exists of this PayloadResponse!");
        return postData.get(key-1).deletePayload(object);
    }

    public int checkPayload(int key, JSONObject object) {
        return postData.get(key-1).anyMatchPayload(object);
    }

    public void addSchema(int key, Schema schema){
        synchronized (this){
            if(key>postData.size()){
                postData.add(new POSTData());
            }
        }
        postData.get(key-1).setSchema(schema);
    }

    public String getSchema(int key) throws IllegalArgumentException {
        if(key>postData.size()) {
            throw new IllegalArgumentException("No Schema Present for this path");
        }
        return postData.get(key-1).getSchema();
    }
}
*/

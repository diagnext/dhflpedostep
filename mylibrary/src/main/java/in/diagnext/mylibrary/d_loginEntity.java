package in.diagnext.mylibrary;

public class d_loginEntity {

    private String code;
    private String message;
    d_Data data;


    // Getter Methods

    public String getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }

    public d_Data getData() {
        return data;
    }

    // Setter Methods

    public void setCode( String code ) {
        this.code = code;
    }

    public void setMessage( String message ) {
        this.message = message;
    }

    public void setData( d_Data dDataObject) {
        this.data = dDataObject;
    }
}

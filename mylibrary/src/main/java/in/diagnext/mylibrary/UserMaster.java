package in.diagnext.mylibrary;

public class UserMaster {
    public String userId;
    public String userName;
    public String userMobile;
    public boolean gender;
    public String height;
    public String weight;


    public UserMaster()
    {}

    public UserMaster(String userId, String userName, String userMobile, boolean gender, String height, String weight) {
        this.userId = userId;
        this.userName = userName;
        this.userMobile = userMobile;
        this.gender = gender;
        this.height = height;
        this.weight = weight;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getUserMobile() {
        return userMobile;
    }

    public void setUserMobile(String userMobile) {
        this.userMobile = userMobile;
    }

    public boolean isGender() {
        return gender;
    }

    public void setGender(boolean gender) {
        this.gender = gender;
    }

    public String getHeight() {
        return height;
    }

    public void setHeight(String height) {
        this.height = height;
    }

    public String getWeight() {
        return weight;
    }

    public void setWeight(String weight) {
        this.weight = weight;
    }
}

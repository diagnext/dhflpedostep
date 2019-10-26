package in.diagnext.mylibrary;

public class d_WeightMaster {
    public String weightMasterId;
    public String userId;
    public long date;
    public String weight;

    public d_WeightMaster()
    {}

    public d_WeightMaster(String weightMasterId, String userId, long date, String weight) {
        this.weightMasterId = weightMasterId;
        this.userId = userId;
        this.date = date;
        this.weight = weight;
    }

    public String getWeightMasterId() {
        return weightMasterId;
    }

    public void setWeightMasterId(String weightMasterId) {
        this.weightMasterId = weightMasterId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public long getDate() {
        return date;
    }

    public void setDate(long date) {
        this.date = date;
    }

    public String getWeight() {
        return weight;
    }

    public void setWeight(String weight) {
        this.weight = weight;
    }
}

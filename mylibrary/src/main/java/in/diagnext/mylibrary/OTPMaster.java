package in.diagnext.mylibrary;

public class OTPMaster {

    String OTPMasterId;
    String mobile;
    String otp;

    public OTPMaster()
    {}

    public OTPMaster(String OTPMasterId, String mobile, String otp) {
        this.OTPMasterId = OTPMasterId;
        this.mobile = mobile;
        this.otp = otp;
    }

    public String getOTPMasterId() {
        return OTPMasterId;
    }

    public void setOTPMasterId(String OTPMasterId) {
        this.OTPMasterId = OTPMasterId;
    }

    public String getMobile() {
        return mobile;
    }

    public void setMobile(String mobile) {
        this.mobile = mobile;
    }

    public String getOtp() {
        return otp;
    }

    public void setOtp(String otp) {
        this.otp = otp;
    }
}

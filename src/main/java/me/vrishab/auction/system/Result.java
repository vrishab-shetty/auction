package me.vrishab.auction.system;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor()
@AllArgsConstructor()
public class Result {

    private boolean flag;

    private String message;

    private String errorCode;

    private Object data;

    public Result(boolean flag, String message) {
        this.flag = flag;
        this.message = message;
    }

    public Result(boolean flag, String message, Object data) {
        this.flag = flag;
        this.message = message;
        this.data = data;
    }

    public Result(boolean flag, String message, String errorCode) {
        this.flag = flag;
        this.message = message;
        this.errorCode = errorCode;
    }
}

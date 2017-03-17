/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package pawelz.pl.googledrivepersonalaccount.util;

import com.fasterxml.jackson.annotation.JsonFormat;

/**
 *
 * @author pawelz
 */
@JsonFormat(shape = JsonFormat.Shape.OBJECT)
public enum FileDictionary {
    FILEID("fileId", "Id of a Google file"),
    NAME("name", "Name of a file"),
    SHARES("shares", "List of shares"),
    PARENTID("parentId", "Id of a parent Google file"),
    EMAIL("email","User e-mail"),
    ROLE("role","Share role"),
    TYPE("type","Share type"),
    SENDEMAIL("sendEmail", "If sending email needed for this operation?"),
    EMAILMESSAGE("emailMessage", "Message for e-mail");

    private final String code;
    private final String description;

    FileDictionary(String code, String description) {
        this.code = code;
        this.description = description;
    }

    public String getCode() {
        return code;
    }
    
    public String getDescription() {
        return description;
    }
}

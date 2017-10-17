package org.feedhenry.mcp.mcp_demo.model;

import com.feedhenry.sdk.sync.FHSyncUtils;

import org.json.JSONObject;

import java.io.Serializable;

/**
 * Created by summers on 10/17/17.
 */

public class ShoppingItem implements Serializable, Comparable<ShoppingItem> {

    private String id;
    private String name;
    private String created;

    public ShoppingItem() {
    }

    public ShoppingItem(String id, String name, String created) {
        this.id = id;
        this.name = name;
        this.created = created;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCreated() {
        return created;
    }

    public String toString() {
        return name;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ShoppingItem that = (ShoppingItem) o;

        if (id != null ? !id.equals(that.id) : that.id != null) return false;
        if (name != null ? !name.equals(that.name) : that.name != null)
            return false;
        return !(created != null ? !created.equals(that.created) : that.created != null);

    }

    @Override
    public int hashCode() {
        int result = id != null ? id.hashCode() : 0;
        result = 31 * result + (name != null ? name.hashCode() : 0);
        result = 31 * result + (created != null ? created.hashCode() : 0);
        return result;
    }

    @Override
    public int compareTo(ShoppingItem another) {

        if (another == null) {
            return 1;
        }

        int compareResult = compareStrings(created, another.created);
        if (compareResult == 0) {
            compareResult = compareStrings(name, another.name);
            if (compareResult == 0) {
                compareResult = compareStrings(id, another.id);
            }
        }

        return -1 * compareResult;

    }

    private int compareStrings(String first, String second) {
        if (first == null) {
            if (second == null) {
                return 0;
            } else {
                return -1;
            }
        } else {
            return first.compareTo(second);
        }
    }

    public long getFHhash() {
        try {
            JSONObject create = new JSONObject();
            create.put("name", this.name);
            create.put("created", this.created);
            return FHSyncUtils.generateHash(create.toString()).hashCode();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

}

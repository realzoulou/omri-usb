package eu.hradio.httprequestwrapper.dtos;

import java.io.*;

public class Pageable implements Serializable
{
    private static final long serialVersionUID = -491075691046811508L;
    private Sort sort;
    private int pageSize;
    private int pageNumber;
    private int offset;
    private boolean unpaged;
    private boolean paged;
    
    public Sort getSort() {
        return this.sort;
    }
    
    public void setSort(final Sort sort) {
        this.sort = sort;
    }
    
    public int getPageSize() {
        return this.pageSize;
    }
    
    public void setPageSize(final int pageSize) {
        this.pageSize = pageSize;
    }
    
    public int getPageNumber() {
        return this.pageNumber;
    }
    
    public void setPageNumber(final int pageNumber) {
        this.pageNumber = pageNumber;
    }
    
    public int getOffset() {
        return this.offset;
    }
    
    public void setOffset(final int offset) {
        this.offset = offset;
    }
    
    public boolean getUnpaged() {
        return this.unpaged;
    }
    
    public void setUnpaged(final boolean unpaged) {
        this.unpaged = unpaged;
    }
    
    public boolean getPaged() {
        return this.paged;
    }
    
    public void setPaged(final boolean paged) {
        this.paged = paged;
    }
}

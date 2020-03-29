package eu.hradio.httprequestwrapper.dtos.recommendation;

import java.io.*;
import eu.hradio.httprequestwrapper.dtos.*;

public class RecommendationList implements Serializable
{
    private static final long serialVersionUID = 7397626482051814710L;
    private Recommendation[] content;
    private Pageable pageable;
    private boolean last;
    private int totalElements;
    private int totalPages;
    private Sort sort;
    private int numberOfElements;
    private boolean first;
    private int size;
    private int number;
    
    public Pageable getPageable() {
        return this.pageable;
    }
    
    public void setPageable(final Pageable pageable) {
        this.pageable = pageable;
    }
    
    public boolean getLast() {
        return this.last;
    }
    
    public void setLast(final boolean last) {
        this.last = last;
    }
    
    public int getTotalElements() {
        return this.totalElements;
    }
    
    public void setTotalElements(final int totalElements) {
        this.totalElements = totalElements;
    }
    
    public int getTotalPages() {
        return this.totalPages;
    }
    
    public void setTotalPages(final int totalPages) {
        this.totalPages = totalPages;
    }
    
    public Sort getSort() {
        return this.sort;
    }
    
    public void setSort(final Sort sort) {
        this.sort = sort;
    }
    
    public int getNumberOfElements() {
        return this.numberOfElements;
    }
    
    public void setNumberOfElements(final int numberOfElements) {
        this.numberOfElements = numberOfElements;
    }
    
    public boolean getFirst() {
        return this.first;
    }
    
    public void setFirst(final boolean first) {
        this.first = first;
    }
    
    public int getSize() {
        return this.size;
    }
    
    public void setSize(final int size) {
        this.size = size;
    }
    
    public int getNumber() {
        return this.number;
    }
    
    public void setNumber(final int number) {
        this.number = number;
    }
    
    public Recommendation[] getContent() {
        return this.content;
    }
    
    public void setContent(final Recommendation[] content) {
        this.content = content;
    }
}

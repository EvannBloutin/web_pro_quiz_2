package com.campuslibrary.util;

import java.util.List;

/** A page of results (used by every paginated list). */
public class Page<T> {
    private final List<T> items;
    private final long total;
    private final int page;
    private final int size;

    public Page(List<T> items, long total, int page, int size) {
        this.items = items; this.total = total; this.page = page; this.size = size;
    }
    public List<T> getItems() { return items; }
    public long getTotal() { return total; }
    public int getPage() { return page; }
    public int getSize() { return size; }
    public int getPages() { return (int) Math.max(1, Math.ceil(total / (double) size)); }
    public boolean isHasPrev() { return page > 1; }
    public boolean isHasNext() { return page < getPages(); }
    public boolean isEmpty() { return items.isEmpty(); }
}

package com.example.user.testone;

/**
 * Created by Antonio Gonzalez on 4/17/15.
 */
public class PageTable {

    class TableEntry{
        public boolean dirtyBit;
        public boolean validBit;
        public int frame;
    }

    static final int IN_PAGE_OFFSET_MASK = 7;

    // RETURN CODES
    static final int PAGE_FAULT = -1;


    /*
    The actual page table. Each entry has three fields: frame number,
    dirty bit and valid bit.
    |_real address_|_valid bit_|_dirty bit_|
    |______________|___________|___________|
    |______________|___________|___________|
     */
    TableEntry [] table;

    int pageNumber = 0;
    int referenceCount = 0;
    int pageFaultCount = 0;
    int hitCount = 0;
    int faultAddress = 0;

    /**
     * Allocates the correct page table size according to the
     * program size
     * @param progSize
     */
    public void setTableSize(int progSize){
        if(progSize%8 > 0) {
            table = new TableEntry[(progSize/8) + 1];
        }
        else {
            table = new TableEntry[progSize/8];
        }
    }

    /**
     * Invoked to do a table look up operation. Translates a logical address
     * into a physical address
     * @param logicAddress the logical address
     * @return the physical address
     */
    public int lookUp(int logicAddress){
        referenceCount++;
        faultAddress = logicAddress;
        pageNumber = logicAddress >> 3;
        if(table[pageNumber].validBit) {
            hitCount++;
            return (logicAddress & IN_PAGE_OFFSET_MASK) | (table[pageNumber].frame << 3);
        }else {
            pageFaultCount++;
            return PAGE_FAULT;
        }
    }

    /**
     * Gets the dirty bit for a page table entry
     * @param index entry index value
     * @return true if bit is set. False otherwise.
     */
    public boolean dirtyBitSet(int index){
        return table[index].dirtyBit;
    }

    /**
     * Gets the valid bit for a page table entry
     * @param index entry index value
     * @return true if bit is set. False otherwise
     */
    public boolean validBitSet(int index){
        return table[index].validBit;
    }

    public void clearEntry(int index){
        table[index].dirtyBit = false;
        table[index].validBit = false;
        table[index].frame = 0;
    }

    /**
     * Returns the hit to reference count ratio. Used for statistical
     * purposes.
     * @return
     */
    public double getHitRatio(){
        return (double) hitCount / referenceCount;
    }
}

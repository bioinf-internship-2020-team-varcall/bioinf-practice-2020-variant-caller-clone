package com.epam.bioinf.variantcaller.caller;

import htsjdk.samtools.CigarElement;
import htsjdk.samtools.CigarOperator;
import htsjdk.samtools.SAMRecord;
import htsjdk.samtools.reference.IndexedFastaSequenceFile;
import htsjdk.samtools.reference.ReferenceSequence;
import org.apache.commons.compress.utils.Sets;

import java.util.*;

public class Caller {
  private IndexedFastaSequenceFile fastaSequenceFile;
  private List<SAMRecord> samRecords;
  private final HashMap<String, PotentialVariants[]> variants;

  public Caller() {
    variants = new HashMap<>();
  }

//  public PotentialVariants[] call(IndexedFastaSequenceFile fastaSequenceFile, List<SAMRecord> samRecords) {
//    this.fastaSequenceFile = fastaSequenceFile;
//    this.samRecords = samRecords;
//    initVariants();
//    HashMap<String, HashMap<Integer, PotentialVariants>> result = new HashMap<>();
//    variants.keySet().forEach(key -> {
//      Arrays.stream(variants.get(key)).map(el -> )
//    });
//  }

  public void call(IndexedFastaSequenceFile fastaSequenceFile, List<SAMRecord> samRecords) {
    this.fastaSequenceFile = fastaSequenceFile;
    this.samRecords = samRecords;
    initVariants();
  }

  private void initVariants() {
    samRecords.forEach(samRecord -> {
      String contig = samRecord.getContig();
      if (contig != null) {
        if (!variants.containsKey(contig)) {
          variants.put(contig,
              new PotentialVariants[fastaSequenceFile.getSequence(contig).length()]);
        }

        PotentialVariants[] variantsByContig = variants.get(contig);
        int start = samRecord.getStart();
        ReferenceSequence subsequenceAt =
            fastaSequenceFile.getSubsequenceAt(contig, start, samRecord.getEnd());
        byte[] subsequenceBases = subsequenceAt.getBases();

        int refInd = 0;
        int readInd = 0;
        byte[] readBases = samRecord.getReadBases();
        for (CigarElement cigarElement : samRecord.getCigar().getCigarElements()) {
          CigarOperator operator = cigarElement.getOperator();
          int length = cigarElement.getLength();
          if (operator.isAlignment()) {
            for (int j = 0; j < length - 1; j++) {
              if (variantsByContig[start + refInd] == null) {
                variantsByContig[start + refInd] = new PotentialVariants(subsequenceBases[refInd]);
              } else {
                var c = readBases[readInd];
                if (variantsByContig[start + refInd].getRefChar() != c) {
                  variantsByContig[start + refInd].addPotentialVariant(c);
                }
              }
              refInd++;
              readInd++;
            }
          } else if (operator.equals(CigarOperator.D)) {
            refInd += length + 1;
            readInd++;
          } else if (operator.equals(CigarOperator.I)) {
            readInd += length + 1;
            refInd++;
          }
        }
      }
    });
  }
}

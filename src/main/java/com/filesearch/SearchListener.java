package com.filesearch;

public interface SearchListener {

      void error(String msg);
      void log(String msg);
      void setProgress(int total,int amt,String message);
}

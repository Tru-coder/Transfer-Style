package com.example.transferstylerebuildmaven.commons;

public enum RequestState {
    Processing {
        @Override
        public String toString(){
            return "processing";
        }
    },
    Done {
        @Override
        public String toString() {
            return "done";
        }
    },
    Error {
        @Override
        public String toString() {
            return "error";
        }
    }
}

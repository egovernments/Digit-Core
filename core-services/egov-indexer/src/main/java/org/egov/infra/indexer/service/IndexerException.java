package org.egov.infra.indexer.service;

public class IndexerException extends RuntimeException {

    private String targetIndexNames;

    public IndexerException(String message, Throwable cause) {
        super(message, cause);
    }

    public String getTargetIndexNames() {
        return targetIndexNames;
    }

    public void setTargetIndexNames(String targetIndexNames) {
        this.targetIndexNames = targetIndexNames;
    }
}

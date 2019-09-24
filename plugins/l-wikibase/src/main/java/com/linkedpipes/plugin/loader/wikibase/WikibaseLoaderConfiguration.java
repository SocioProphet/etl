package com.linkedpipes.plugin.loader.wikibase;

import com.linkedpipes.etl.executor.api.v1.rdf.RdfToPojo;

@RdfToPojo.Type(iri = WikibaseLoaderVocabulary.CONFIG)
public class WikibaseLoaderConfiguration {

    @RdfToPojo.Property(iri = WikibaseLoaderVocabulary.HAS_ENDPOINT)
    private String endpoint;

    @RdfToPojo.Property(iri = WikibaseLoaderVocabulary.HAS_USERNAME)
    private String userName;

    @RdfToPojo.Property(iri = WikibaseLoaderVocabulary.HAS_PASSWORD)
    private String password;

    @RdfToPojo.Property(iri = WikibaseLoaderVocabulary.HAS_SITE_IRI)
    private String siteIri;

    @RdfToPojo.Property(iri = WikibaseLoaderVocabulary.HAS_SPARQL_URL)
    private String sparqlUrl;

    @RdfToPojo.Property(iri = WikibaseLoaderVocabulary.HAS_REF_PROPERTY)
    private String refProperty;

    @RdfToPojo.Property(iri = WikibaseLoaderVocabulary.HAS_EDIT_TIME)
    private int averageTimePerEdit = 2000;

    public WikibaseLoaderConfiguration() {
    }

    public String getEndpoint() {
        return endpoint;
    }

    public void setEndpoint(String endpoint) {
        this.endpoint = endpoint;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getSiteIri() {
        return siteIri;
    }

    public void setSiteIri(String siteIri) {
        this.siteIri = siteIri;
    }

    public String getSparqlUrl() {
        return sparqlUrl;
    }

    public void setSparqlUrl(String sparqlUrl) {
        this.sparqlUrl = sparqlUrl;
    }

    public String getRefProperty() {
        return refProperty;
    }

    public void setRefProperty(String refProperty) {
        this.refProperty = refProperty;
    }

    public int getAverageTimePerEdit() {
        return averageTimePerEdit;
    }

    public void setAverageTimePerEdit(int averageTimePerEdit) {
        this.averageTimePerEdit = averageTimePerEdit;
    }

}

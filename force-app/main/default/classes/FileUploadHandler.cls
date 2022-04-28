public with sharing class FileUploadHandler {

    private final static String csvMimeType = 'text/csv';
    private final static String excelMimeType = 'application/vnd.excel';

    @AuraEnabled
    public static String processFileUpload(String contentVersionId) {
        Functions.Function fileUploadFunction = Functions.Function.get('functiondemo.largefileupload');

        Map<String, Object> payload = new Map<String, Object>{
                'contentVersionId' => contentVersionId
        };

        Functions.FunctionInvocation invocation = fileUploadFunction.invoke(JSON.serialize(payload));

        System.debug(JSON.serializePretty(invocation));

        String response = invocation.getResponse();

        // TODO do something with response

        return response;
    }
    @AuraEnabled
    public static String processFileUploadV1(String contentVersionId) {
        Functions.Function fileUploadFunction = Functions.Function.get('functiondemo.csvcartupload');

        ContentVersion version = [SELECT PathOnClient, VersionData FROM ContentVersion WHERE Id = :contentVersionId];

        String contentType = getContentTypeFromVersion(version);
        String base64Data = getBase64EncodedDataFromVersion(version);

        Map<String, Object> payload = new Map<String, Object>{
                'contentType' => contentType,
                'base64EncodedData' => base64Data
        };

        Functions.FunctionInvocation invocation = fileUploadFunction.invoke(JSON.serialize(payload));

        System.debug(JSON.serializePretty(invocation));

        String response = invocation.getResponse();

        // TODO do something with response

        return response;
    }

    private static String getBase64EncodedDataFromVersion(ContentVersion contentVersion) {
        return EncodingUtil.base64Encode(contentVersion.VersionData);
    }

    private static String getContentTypeFromVersion(ContentVersion contentVersion) {
        if (contentVersion.PathOnClient.endsWithIgnoreCase('csv')) {
            return csvMimeType;
        } else if (contentVersion.PathOnClient.endsWithIgnoreCase('xlsx')) {
            return excelMimeType;
        }

        return null; // TODO probably better to throw an exception here!
    }

    @AuraEnabled
    public static String createShare(String contentVersionId) {
        System.debug('CVID is: ' + contentVersionId);

        createShareInternal(contentVersionId);

        return '';
    }


    private static void createShareInternal(String cvId) {
        System.debug('CVID is: ' + cvId);

        User user = [SELECT Id FROM User WHERE Username LIKE 'cloud@%' LIMIT 1];
        Id conDocId = [SELECT ContentDocumentId FROM ContentVersion WHERE Id =:cvId].ContentDocumentId;

        ContentDocumentLink cdl = new ContentDocumentLink();
        cdl.ContentDocumentId = conDocId;
        cdl.LinkedEntityId = user.Id;
        cdl.ShareType = 'V';
        insert cdl;
    }

}
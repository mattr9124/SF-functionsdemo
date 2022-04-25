public with sharing class FileUploadHandlerAsync {

    private final static String csvMimeType = 'text/csv';
    private final static String excelMimeType = 'application/vnd.excel';

    @AuraEnabled
    public static String processFileUpload(String contentVersionId) {
        Functions.Function fileUploadFunction = Functions.Function.get('functiondemo.largefileupload');

        createShare(contentVersionId);

        Map<String, Object> payload = new Map<String, Object>{
                'contentVersionId' => contentVersionId
        };

        Functions.FunctionCallback callback = new FileUploadCallback();

        Functions.FunctionInvocation invocation = fileUploadFunction.invoke(JSON.serialize(payload), callback);

        System.debug(JSON.serializePretty(invocation));

        String response = invocation.getResponse();

        // TODO do something with response

        return response;
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

    class FileUploadCallback implements Functions.FunctionCallback {
        public void handleResponse(Functions.FunctionInvocation invocation) {

            System.debug(JSON.serializePretty(invocation));

        }
    }

}
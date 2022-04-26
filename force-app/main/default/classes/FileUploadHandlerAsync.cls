public with sharing class FileUploadHandlerAsync {

    private final static String csvMimeType = 'text/csv';
    private final static String excelMimeType = 'application/vnd.excel';

    @AuraEnabled
    public static String processFileUpload(String contentVersionId) {
        Functions.Function fileUploadFunction = Functions.Function.get('functiondemo.largefileupload');

        Id linkId = createShareInternal(contentVersionId);

        Map<String, Object> payload = new Map<String, Object>{
                'contentVersionId' => contentVersionId
        };

        Functions.FunctionCallback callback = new FileUploadCallback(linkId);

        Functions.FunctionInvocation invocation = fileUploadFunction.invoke(JSON.serialize(payload), callback);

        System.debug(JSON.serializePretty(invocation));

        String response = JSON.serialize(invocation);

        // TODO do something with response

        return response;
    }

    private static Id createShareInternal(String cvId) {
        User user = [SELECT Id FROM User WHERE Username LIKE 'cloud@%' LIMIT 1];
        Id conDocId = [SELECT ContentDocumentId FROM ContentVersion WHERE Id =:cvId].ContentDocumentId;

        ContentDocumentLink cdl = new ContentDocumentLink();
        cdl.ContentDocumentId = conDocId;
        cdl.LinkedEntityId = user.Id;
        cdl.ShareType = 'V';
        insert cdl;

        return cdl.Id;
    }

    class FileUploadCallback implements Functions.FunctionCallback {
        final Id linkId;

        FileUploadCallback(Id linkId) {
            this.linkId = linkId;
        }

        public void handleResponse(Functions.FunctionInvocation invocation) {
            System.debug(JSON.serializePretty(invocation));

            UploadEvent__e event = new UploadEvent__e();
            event.Payload__c = invocation.getResponse();
            Database.SaveResult result = EventBus.publish(event);

            if (!result.success) {
                for(Database.Error error : result.getErrors()) {
                    System.debug(LoggingLevel.ERROR, error.message);
                }
            }

            delete new ContentDocumentLink(Id = this.linkId);
        }
    }

}
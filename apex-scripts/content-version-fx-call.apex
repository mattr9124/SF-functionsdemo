Id cvId = '0687a000001gikmAAA';

Functions.Function fileUploadFunction = Functions.Function.get('functiondemo.largefileupload');

String payload = '{"contentVersionId" : "' + cvId + '"}';

Functions.FunctionInvocation invocation = fileUploadFunction.invoke(payload);

System.debug(JSON.serializePretty(invocation));

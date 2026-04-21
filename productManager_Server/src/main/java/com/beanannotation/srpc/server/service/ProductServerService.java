package com.beanannotation.srpc.server.service;

import com.beanannotation.ProductServiceGrpc;
import net.devh.boot.grpc.server.service.GrpcService;

@GrpcService
public class ProductServerService extends ProductServiceGrpc.ProductServiceImplBase {

}

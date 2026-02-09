package com.shadsluiter.ordersapp.models;

import java.util.ArrayList;
import java.util.List;

/**
 * Converters class for converting between UserModel and UserEntity.
 * Provides static methods for conversion in both directions.
 * UserModel represents the data used in the application layer.
 * UserEntity represents the data stored in the database.
 */
public class Converters {
    
    public static UserEntity userModelToUserEntity(UserModel userModel) {
        UserEntity userEntity = new UserEntity();
        userEntity.setId(Long.parseLong(userModel.getId()));
        userEntity.setUserName(userModel.getUserName());
        userEntity.setPassword(userModel.getPassword());
        return userEntity;
    }

    public static UserModel userEntityToUserModel(UserEntity userEntity) {
        UserModel userModel = new UserModel();
        userModel.setId(String.valueOf(userEntity.getId()));
        userModel.setUserName(userEntity.getUserName());
        userModel.setPassword(userEntity.getPassword());
        return userModel;
    }

public static OrderEntity orderModelToOrderEntity(OrderModel model) {
    OrderEntity entity = new OrderEntity();

    if (model.getId() != null) {
        entity.setId(Long.parseLong(model.getId()));
    }

    entity.setDate(model.getDate());
    entity.setCustomerid(model.getCustomerid());
    entity.setNotes(model.getNotes());

    entity.setPictureUrl(model.getPictureUrl());  

    return entity;
}


  public static OrderModel orderEntityToOrderModel(OrderEntity entity) {
    OrderModel model = new OrderModel();
    model.setId( String.valueOf(entity.getId()) );
    model.setDate(entity.getDate());
    model.setCustomerid(entity.getCustomerid());
    model.setNotes(entity.getNotes());
    model.setPictureUrl(entity.getPictureUrl()); // ✅
    return model;
}


    // convert list
    public static List<OrderModel> convertToModels(List<OrderEntity> orderEntities) {
        List<OrderModel> orderModels = new ArrayList<>();
        for (OrderEntity orderEntity : orderEntities) {
            orderModels.add(orderEntityToOrderModel(orderEntity));
        }
        return orderModels;
    }

    public static List<OrderEntity> convertToEntities(List<OrderModel> orderModels) {
        List<OrderEntity> orderEntities = new ArrayList<>();
        for (OrderModel orderModel : orderModels) {
            orderEntities.add(orderModelToOrderEntity(orderModel));
        }
        return orderEntities;
    }
    
}

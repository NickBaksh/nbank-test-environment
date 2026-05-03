package requests.skelethon.interfaces;

import models.BaseModel;

public interface CrudEndpointInterface {
    Object create();
    Object create(BaseModel model); //POST запрос
    Object read(); //GET запрос
    Object read(long id); //GET запрос где в URL передаем id
    Object update(BaseModel model); //PUT запрос
    Object delete(long id);
}
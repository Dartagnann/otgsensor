# coding=utf-8
import json
from importlib import reload

from flask import Flask
from flask import request
# from flask_script import Manager
import os
from flask_sqlalchemy import SQLAlchemy
from sqlalchemy import MetaData
from sqlalchemy import create_engine
from sqlalchemy.orm import *
import sys

reload(sys)
app = Flask(__name__)
app.config['SECRET_KEY'] = 'Oliver'
app.config['SQLALCHEMY_DATABASE_URI'] = 'mysql+pymysql://root:Oliver@localhost/mydb'
app.config['SQLALCHEMY_TRACK_MODIFICATIONS'] = True
app.config['SQLALCHEMY_COMMIT_ON_TEARDOWN'] = True
db = SQLAlchemy(app)
@app.route('/')
def test():
    return 'success'

class userInfoTable(db.Model):
    __tablename__='userInfo'
    uid=db.Column(db.Integer,primary_key=True)
    username=db.Column(db.String,unique=True)
    password=db.Column(db.String)

    def __repr__(self):
        return 'table name is '+self.username

class dataTable(db.Model):
    _tablename='Data'
    id=db.Column(db.Integer,primary_key=True)
    date = db.Column(db.String(100))
    temp = db.Column(db.String(100))
    humidity = db.Column(db.String(100))
    pressure = db.Column(db.String(100))
    illumination = db.Column(db.String(100))
    soil_t = db.Column(db.String(100))
    soil_h = db.Column(db.String(100))
    uv = db.Column(db.String(100))
    longitude = db.Column(db.String(100))
    latitude = db.Column(db.String(100))

    def __repr__(self):
        #return 'table name is ' + self.username
        return 'success'+self.date


# 检查用户登陆
@app.route('/user', methods=['POST'])
def check_user():
    userName = request.form['username']
    haveregisted = userInfoTable.query.filter_by(username=request.form['username']).all()
    obj = userInfoTable.query.filter_by(username=request.form['username']).first()
    if haveregisted.__len__() is not 0:  # 判断是否已被注册
        passwordRight = userInfoTable.query.filter_by(username=request.form['username'],
                                                      password=request.form['password']).all()
        #userId = studentTable.query.filter_by(userName=request.form['username']).id()
        if passwordRight.__len__() is not 0:
            #print (str(userName) + "log success")
            #return '欢迎，'+ str(obj.id)
           return '登录成功'
        else:
            return '1'
    else:
       # print (str(userName) + "log fail")
        return '0'

# 此方法处理用户注册
@app.route('/register', methods=['POST'])
def register():
    userName = request.form['username']
    db.create_all()
    haveregisted = userInfoTable.query.filter_by(username=request.form['username']).all()
    if haveregisted.__len__() is not 0:  # 判断是否已被注册
        return '0'
    student = userInfoTable(username=request.form['username'], password=request.form['password'])
    db.session.add(student)
    db.session.commit()
    return '注册成功'

# 客户端将数据传上服务器，更新服务器上的数据
@app.route('/postdata', methods=['POST'])
def postData():
    db.create_all()
    userName = request.form['username']
    cDate = request.form['date']
    cTemp = request.form['temp']
    cHumidity = request.form['humidity']
    cPressure = request.form['pressure']
    cIllumination = request.form['illumination']
    cSoil_t = request.form['soil_t']
    cSoil_h = request.form['soil_h']
    cUv = request.form['uv']
    cLongitude = request.form['longitude']
    cLatitude = request.form['latitude']
    print('收到了'+userName)
    haveExisted = dataTable.query.filter_by(temp=request.form['temp'],
                                            humidity=request.form['humidity'],
                                            pressure=request.form['pressure'],
                                            illumination=request.form['illumination'],
                                            soil_t=request.form['soil_t'],
                                            soil_h=request.form['soil_h'],
                                            uv=request.form['uv']).all()  # 判断数据库中是否有相同的数据了
    if len(haveExisted) is not 0:
        print ('already exist' + str(len(haveExisted)))
        return '0' #数据已存在

    try:
        cData = dataTable( date=cDate,
                          temp=cTemp,
                          humidity=cHumidity,
                          pressure=cPressure,
                          illumination=cIllumination,
                          soil_t=cSoil_t,
                          soil_h=cSoil_h,
                          uv=cUv,
                          longitude=cLongitude,
                          latitude=cLatitude)
        db.session.add(cData)
        db.session.commit()
    except:
        #db.session.rollback()
        print ('出问题了')
        return '1'
    return '2'

if __name__ == '__main__':
    # manager.run()
    app.run(host='0.0.0.0', port=80,threaded=True)


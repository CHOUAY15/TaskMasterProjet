import { Manager } from "./manager";

export class Team{


    constructor(public id:number ,public name:string,public description: string,public teamNbr:number,public creationDate:Date,public sectionName:string,public manager:Manager){}


}
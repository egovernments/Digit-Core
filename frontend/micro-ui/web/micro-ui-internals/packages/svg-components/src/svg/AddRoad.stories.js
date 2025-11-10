import React from "react";
import { AddRoad } from "./AddRoad";

export default {
  tags: ['autodocs'],
  argTypes: {
    className: {
        options: ['custom-class'],
        control: { type: 'check' },
    }
  },
  title: "AddRoad",
  component: AddRoad,
};

export const Default = () => <AddRoad />;
export const Fill = () => <AddRoad fill="blue" />;
export const Size = () => <AddRoad height="50" width="50" />;
export const CustomStyle = () => <AddRoad style={{ border: "1px solid red" }} />;
export const CustomClassName = () => <AddRoad className="custom-class" />;
export const Clickable = () => <AddRoad onClick={()=>console.log("clicked")} />;

const Template = (args) => <AddRoad {...args}/>;

export const Playground = Template.bind({});
Playground.args = {
  className: 'custom-class',
  style:{ border: "3px solid green" }
};

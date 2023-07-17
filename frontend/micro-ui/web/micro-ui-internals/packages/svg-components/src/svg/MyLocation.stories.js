import React from "react";
import { MyLocation } from "./MyLocation";

export default {
  tags: ['autodocs'],
  argTypes: {
    className: {
        options: ['custom-class'],
        control: { type: 'check' },
    }
  },
  title: "MyLocation",
  component: MyLocation,
};

export const Default = () => <MyLocation />;
export const Fill = () => <MyLocation fill="blue" />;
export const Size = () => <MyLocation height="50" width="50" />;
export const CustomStyle = () => <MyLocation style={{ border: "1px solid red" }} />;
export const CustomClassName = () => <MyLocation className="custom-class" />;

export const Clickable = () => <MyLocation onClick={()=>console.log("clicked")} />;

const Template = (args) => <MyLocation {...args} />;

export const Playground = Template.bind({});
Playground.args = {
  className: "custom-class",
  style: { border: "3px solid green" }
};

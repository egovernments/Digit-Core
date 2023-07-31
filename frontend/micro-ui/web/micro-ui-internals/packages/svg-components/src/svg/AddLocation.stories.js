import React from "react";
import { AddLocation } from "./AddLocation";

export default {
  tags: ['autodocs'],
  argTypes: {
    className: {
        options: ['custom-class'],
        control: { type: 'check' },
    }
  },
  title: "AddLocation",
  component: AddLocation,
};

export const Default = () => <AddLocation />;
export const Fill = () => <AddLocation fill="blue" />;
export const Size = () => <AddLocation height="50" width="50" />;
export const CustomStyle = () => <AddLocation style={{ border: "1px solid red" }} />;
export const CustomClassName = () => <AddLocation className="custom-class" />;
export const Clickable = () => <AddLocation onClick={()=>console.log("clicked")} />;

const Template = (args) => <AddLocation {...args}/>;

export const Playground = Template.bind({});
Playground.args = {
  className: 'custom-class',
  style:{ border: "3px solid green" }
};

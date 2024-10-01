import React from "react";
import { AddBusiness } from "./AddBusiness";

export default {
  tags: ['autodocs'],
  argTypes: {
    className: {
        options: ['custom-class'],
        control: { type: 'check' },
    }
  },
  title: "AddBusiness",
  component: AddBusiness,
};

export const Default = () => <AddBusiness />;
export const Fill = () => <AddBusiness fill="blue" />;
export const Size = () => <AddBusiness height="50" width="50" />;
export const CustomStyle = () => <AddBusiness style={{ border: "1px solid red" }} />;
export const CustomClassName = () => <AddBusiness className="custom-class" />;
export const Clickable = () => <AddBusiness onClick={()=>console.log("clicked")} />;

const Template = (args) => <AddBusiness {...args}/>;

export const Playground = Template.bind({});
Playground.args = {
  className: 'custom-class',
  style:{ border: "3px solid green" }
};

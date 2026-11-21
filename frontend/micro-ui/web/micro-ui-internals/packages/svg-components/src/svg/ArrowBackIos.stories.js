import React from "react";
import { ArrowBackIos } from "./ArrowBackIos";

export default {
  tags: ['autodocs'],
  argTypes: {
    className: {
        options: ['custom-class'],
        control: { type: 'check' },
    }
  },
  title: "ArrowBackIos",
  component: ArrowBackIos,
};

export const Default = () => <ArrowBackIos />;
export const Fill = () => <ArrowBackIos fill="blue" />;
export const Size = () => <ArrowBackIos height="50" width="50" />;
export const CustomStyle = () => <ArrowBackIos style={{ border: "1px solid red" }} />;
export const CustomClassName = () => <ArrowBackIos className="custom-class" />;
export const Clickable = () => <ArrowBackIos onClick={()=>console.log("clicked")} />;

const Template = (args) => <ArrowBackIos {...args}/>;

export const Playground = Template.bind({});
Playground.args = {
  className: 'custom-class',
  style:{ border: "3px solid green" }
};

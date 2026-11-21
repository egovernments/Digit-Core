import React from "react";
import { LocalParking } from "./LocalParking";

export default {
  tags: ['autodocs'],
  argTypes: {
    className: {
        options: ['custom-class'],
        control: { type: 'check' },
    }
  },
  title: "LocalParking",
  component: LocalParking,
};

export const Default = () => <LocalParking />;
export const Fill = () => <LocalParking fill="blue" />;
export const Size = () => <LocalParking height="50" width="50" />;
export const CustomStyle = () => <LocalParking style={{ border: "1px solid red" }} />;
export const CustomClassName = () => <LocalParking className="custom-class" />;

export const Clickable = () => <LocalParking onClick={()=>console.log("clicked")} />;

const Template = (args) => <LocalParking {...args} />;

export const Playground = Template.bind({});
Playground.args = {
  className: "custom-class",
  style: { border: "3px solid green" }
};
